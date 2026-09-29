package com.example.proyek_mdp.UI.XR

import android.content.Context
import android.util.Log
import io.github.sceneview.node.ModelNode
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

/**
 * Mengendalikan tulang (bone) model glTF yang sudah di-rig.
 *
 * Cara kerja:
 *  1. Parse file .glb (JSON chunk + BIN chunk) untuk mengambil hierarki node,
 *     daftar joint pada skin, dan inverseBindMatrices.
 *  2. Hitung rest transform tiap joint (model space).
 *  3. Untuk pose tertentu, ganti rotasi lokal joint yang dipilih, hitung ulang
 *     skinning matrix (jointGlobal * inverseBind), lalu kirim ke Filament lewat
 *     ModelNode.setBonesAsMatrices().
 *
 * Catatan: implementasi ini mengasumsikan mesh berada di root model sehingga
 * skinning matrix = jointGlobal * inverseBind.
 */
class RigPoseController(
    private val context: Context,
    private val assetPath: String
) {
    var jointCount: Int = 0
        private set

    private var parents: IntArray = IntArray(0)
    private val restLocal = ArrayList<FloatArray>()
    private val inverseBind = ArrayList<FloatArray>()
    private val restGlobal = ArrayList<FloatArray>()
    private val nameToSkinIndex = HashMap<String, Int>()

    val isLoaded: Boolean get() = jointCount > 0

    init {
        try {
            parseGlb()
            computeRestGlobals()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal parse $assetPath", e)
        }
    }

    /** Nama tulang -> indeks di dalam skin (untuk pemetaan). */
    fun skinIndex(name: String): Int = nameToSkinIndex[name] ?: -1

    /** Cari indeks tulang berdasarkan awalan nama (mis. "LArm", "RForeArm", "Head"). */
    fun skinIndexByPrefix(prefix: String): Int =
        nameToSkinIndex.entries.firstOrNull { it.key.startsWith(prefix, ignoreCase = true) }?.value ?: -1

    fun parentOf(index: Int): Int = if (index in parents.indices) parents[index] else -1

    fun restGlobalOf(index: Int): FloatArray = restGlobal[index]

    /** Rotasi lokal rest (3x3 row-major) untuk bone tsb. */
    fun restLocalRot(index: Int): FloatArray = rotationFrom4x4(restLocal[index])

    fun firstChildOf(index: Int): Int {
        for (i in 0 until jointCount) if (parents[i] == index) return i
        return -1
    }

    /**
     * Hitung seluruh bone matrix untuk pose tertentu.
     * @param localRotations map skinIndex -> matriks rotasi 3x3 (row-major, 9 float)
     *        yang menggantikan rotasi lokal rest.
     * @return FloatArray berisi jointCount * 16 matrix (column-major, siap upload).
     */
    fun computeBoneMatrices(localRotations: Map<Int, FloatArray>): FloatArray {
        val cache = arrayOfNulls<FloatArray>(jointCount)

        fun globalOf(i: Int): FloatArray {
            cache[i]?.let { return it }
            val local = if (localRotations.containsKey(i)) {
                val t = translationOf(restLocal[i])
                val s = scaleOf(restLocal[i])
                buildTrs(t, localRotations[i]!!, s)
            } else {
                restLocal[i]
            }
            val g = if (parents[i] < 0) local else mulMat(globalOf(parents[i]), local)
            cache[i] = g
            return g
        }

        val out = FloatArray(jointCount * 16)
        for (i in 0 until jointCount) {
            val m = mulMat(globalOf(i), inverseBind[i])
            System.arraycopy(m, 0, out, i * 16, 16)
        }
        return out
    }

    /**
     * Terapkan hasil [computeBoneMatrices] ke seluruh renderable pada model.
     * @return ringkasan hasil (untuk ditampilkan/di-log).
     */
    fun applyBones(modelNode: ModelNode, localRotations: Map<Int, FloatArray>): String {
        if (!isLoaded) {
            Log.w(TAG, "applyBones dipanggil tapi rig belum termuat")
            return "rig kosong"
        }
        val data = computeBoneMatrices(localRotations)
        val buffer: FloatBuffer = ByteBuffer
            .allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }

        var ok = 0
        var err = 0
        var lastErr = ""
        modelNode.renderableNodes.forEach { renderable ->
            try {
                // buffer dipakai ulang per renderable -> reset posisi
                buffer.position(0)
                renderable.setBonesAsMatrices(buffer, jointCount, 0)
                ok++
            } catch (e: Exception) {
                err++
                lastErr = e.message ?: e.javaClass.simpleName
                Log.e(TAG, "setBonesAsMatrices gagal pada renderable", e)
            }
        }
        val summary = "bones ok=$ok err=$err renderables=${modelNode.renderableNodes.size} $lastErr"
        Log.d(TAG, summary)
        return summary
    }

    /** Kembalikan ke rest pose (tanpa override). */
    fun reset(modelNode: ModelNode) = applyBones(modelNode, emptyMap())

    /** Kembalikan semua tulang ke transform rest (dipakai saat tidak ada orang). */
    fun resetViaNodes(modelNode: ModelNode): String {
        if (!isLoaded) return "rig kosong"
        val instance = modelNode.modelInstance
        if (instance.skinCount <= 0) return "tidak ada skin"
        val joints = instance.getJointsAt(0)
        val tm = modelNode.engine.transformManager
        var ok = 0
        for (i in joints.indices) {
            if (i < jointCount) {
                try {
                    tm.setTransform(tm.getInstance(joints[i]), restLocal[i])
                    ok++
                } catch (_: Exception) {
                }
            }
        }
        return "reset ok=$ok"
    }

    /**
     * Pendekatan alternatif: ubah transform NODE tulang lewat TransformManager.
     * SceneView memanggil `animator.updateBoneMatrices()` tiap frame sehingga
     * `setBonesAsMatrices` akan ditimpa; dengan mengubah node, updateBoneMatrices
     * akan menghitung skinning dari transform node tersebut.
     */
    fun applyBonesViaNodes(modelNode: ModelNode, localRotations: Map<Int, FloatArray>): String {
        if (!isLoaded) return "rig kosong"
        val instance = modelNode.modelInstance
        if (instance.skinCount <= 0) return "tidak ada skin"

        val joints = instance.getJointsAt(0)
        val tm = modelNode.engine.transformManager

        var ok = 0
        var err = 0
        var lastErr = ""
        localRotations.forEach { (bone, rot) ->
            if (bone in joints.indices) {
                try {
                    val local = buildTrs(translationOf(restLocal[bone]), rot, scaleOf(restLocal[bone]))
                    val ti = tm.getInstance(joints[bone])
                    tm.setTransform(ti, local)
                    ok++
                } catch (e: Exception) {
                    err++
                    lastErr = e.message ?: e.javaClass.simpleName
                }
            }
        }
        val summary = "nodes ok=$ok err=$err joints=${joints.size} $lastErr"
        Log.d(TAG, summary)
        return summary
    }

    // =========================================================
    // PARSING
    // =========================================================

    private fun parseGlb() {
        val bytes = context.assets.open(assetPath).use { it.readBytes() }
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val magic = bb.int
        require(magic == 0x46546C67) { "Bukan file GLB" }
        bb.int // version
        bb.int // length

        var json: JSONObject? = null
        var bin: ByteBuffer? = null

        while (bb.remaining() >= 8) {
            val chunkLen = bb.int
            val chunkType = bb.int
            val start = bb.position()
            if (chunkType == 0x4E4F534A) { // JSON
                val jsonBytes = ByteArray(chunkLen)
                bb.get(jsonBytes)
                json = JSONObject(String(jsonBytes, Charsets.UTF_8))
            } else if (chunkType == 0x004E4942) { // BIN
                val binBytes = ByteArray(chunkLen)
                bb.get(binBytes)
                bin = ByteBuffer.wrap(binBytes).order(ByteOrder.LITTLE_ENDIAN)
            } else {
                bb.position(start + chunkLen)
            }
        }

        val gltf = json ?: error("JSON chunk tidak ditemukan")
        val binBuf = bin ?: error("BIN chunk tidak ditemukan")

        val nodes = gltf.getJSONArray("nodes")
        val skin = gltf.getJSONArray("skins").getJSONObject(0)
        val jointsArr = skin.getJSONArray("joints")
        jointCount = jointsArr.length()

        // parent map (dalam indeks node glTF)
        val parentOfNode = HashMap<Int, Int>()
        for (i in 0 until nodes.length()) {
            val children = nodes.getJSONObject(i).optJSONArray("children") ?: continue
            for (c in 0 until children.length()) {
                parentOfNode[children.getInt(c)] = i
            }
        }

        // skin slot -> node index glTF
        val jointNodeIndex = IntArray(jointCount) { jointsArr.getInt(it) }
        val nodeToSkinIndex = HashMap<Int, Int>()
        for (i in 0 until jointCount) nodeToSkinIndex[jointNodeIndex[i]] = i

        // inverse bind matrices
        val ibmAccessorIndex = skin.getInt("inverseBindMatrices")
        val ibmData = readAccessorFloats(gltf, binBuf, ibmAccessorIndex, jointCount * 16)

        // local rest + parent (dalam indeks skin)
        parents = IntArray(jointCount) { -1 }
        restLocal.clear()
        inverseBind.clear()
        nameToSkinIndex.clear()

        for (i in 0 until jointCount) {
            val nodeIdx = jointNodeIndex[i]
            val node = nodes.getJSONObject(nodeIdx)
            restLocal.add(nodeLocalMatrix(node))

            val ibm = FloatArray(16)
            System.arraycopy(ibmData, i * 16, ibm, 0, 16)
            inverseBind.add(ibm)

            nameToSkinIndex[node.optString("name", "joint_$i")] = i

            val p = parentOfNode[nodeIdx]
            if (p != null && nodeToSkinIndex.containsKey(p)) {
                parents[i] = nodeToSkinIndex[p]!!
            }
        }
    }

    private fun computeRestGlobals() {
        restGlobal.clear()
        for (i in 0 until jointCount) {
            val g = if (parents[i] < 0) restLocal[i] else mulMat(restGlobal[parents[i]], restLocal[i])
            restGlobal.add(g)
        }
    }

    /** Baca accessor float (mis. MAT4 inverseBindMatrices) dari BIN chunk. */
    private fun readAccessorFloats(
        gltf: JSONObject,
        bin: ByteBuffer,
        accessorIndex: Int,
        expectedCount: Int
    ): FloatArray {
        val accessor = gltf.getJSONArray("accessors").getJSONObject(accessorIndex)
        val count = accessor.getInt("count")
        val bufferViewIndex = accessor.getInt("bufferView")
        val bufferView = gltf.getJSONArray("bufferViews").getJSONObject(bufferViewIndex)
        val byteOffset = bufferView.optInt("byteOffset", 0) + accessor.optInt("byteOffset", 0)

        val out = FloatArray(expectedCount)
        val dup = bin.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        dup.position(byteOffset)
        val available = minOf(count * 16, expectedCount)
        for (i in 0 until available) out[i] = dup.float
        return out
    }

    private fun nodeLocalMatrix(node: JSONObject): FloatArray {
        node.optJSONArray("matrix")?.let { m ->
            return FloatArray(16) { m.getDouble(it).toFloat() }
        }
        val t = node.optJSONArray("translation")?.let {
            floatArrayOf(it.getDouble(0).toFloat(), it.getDouble(1).toFloat(), it.getDouble(2).toFloat())
        } ?: floatArrayOf(0f, 0f, 0f)

        val q = node.optJSONArray("rotation")?.let {
            floatArrayOf(
                it.getDouble(0).toFloat(), it.getDouble(1).toFloat(),
                it.getDouble(2).toFloat(), it.getDouble(3).toFloat()
            )
        } ?: floatArrayOf(0f, 0f, 0f, 1f)

        val s = node.optJSONArray("scale")?.let {
            floatArrayOf(it.getDouble(0).toFloat(), it.getDouble(1).toFloat(), it.getDouble(2).toFloat())
        } ?: floatArrayOf(1f, 1f, 1f)

        val rot3 = quatToMat3(q)
        return buildTrs(t, rot3, s)
    }

    // =========================================================
    // MATH (column-major, 16 float)
    // =========================================================

    private fun translationOf(m: FloatArray) = floatArrayOf(m[12], m[13], m[14])

    private fun scaleOf(m: FloatArray): FloatArray {
        val sx = sqrt(m[0] * m[0] + m[1] * m[1] + m[2] * m[2])
        val sy = sqrt(m[4] * m[4] + m[5] * m[5] + m[6] * m[6])
        val sz = sqrt(m[8] * m[8] + m[9] * m[9] + m[10] * m[10])
        return floatArrayOf(sx, sy, sz)
    }

    private fun buildTrs(t: FloatArray, r: FloatArray, s: FloatArray): FloatArray {
        // r: 3x3 row-major (9)
        val m = FloatArray(16)
        m[0] = r[0] * s[0]; m[1] = r[3] * s[0]; m[2] = r[6] * s[0]
        m[4] = r[1] * s[1]; m[5] = r[4] * s[1]; m[6] = r[7] * s[1]
        m[8] = r[2] * s[2]; m[9] = r[5] * s[2]; m[10] = r[8] * s[2]
        m[12] = t[0]; m[13] = t[1]; m[14] = t[2]; m[15] = 1f
        return m
    }

    private fun quatToMat3(q: FloatArray): FloatArray {
        val x = q[0]; val y = q[1]; val z = q[2]; val w = q[3]
        val xx = x * x; val yy = y * y; val zz = z * z
        val xy = x * y; val xz = x * z; val yz = y * z
        val wx = w * x; val wy = w * y; val wz = w * z
        return floatArrayOf(
            1 - 2 * (yy + zz), 2 * (xy - wz), 2 * (xz + wy),
            2 * (xy + wz), 1 - 2 * (xx + zz), 2 * (yz - wx),
            2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (xx + yy)
        )
    }

    private fun mulMat(a: FloatArray, b: FloatArray): FloatArray {
        val r = FloatArray(16)
        for (c in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) sum += a[k * 4 + row] * b[c * 4 + k]
                r[c * 4 + row] = sum
            }
        }
        return r
    }

    companion object {
        private const val TAG = "RigPose"

        /** Ambil rotasi 3x3 (row-major) dari matriks 4x4 column-major. */
        fun rotationFrom4x4(m: FloatArray): FloatArray = floatArrayOf(
            m[0], m[4], m[8],
            m[1], m[5], m[9],
            m[2], m[6], m[10]
        )

        fun mat3Mul(a: FloatArray, b: FloatArray): FloatArray {
            val r = FloatArray(9)
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    var s = 0f
                    for (k in 0 until 3) s += a[i * 3 + k] * b[k * 3 + j]
                    r[i * 3 + j] = s
                }
            }
            return r
        }

        fun mat3Transpose(a: FloatArray): FloatArray =
            floatArrayOf(a[0], a[3], a[6], a[1], a[4], a[7], a[2], a[5], a[8])

        /** Rotasi yang memetakan vektor a ke vektor b (row-major 3x3). */
        fun rotationFromTo(a: FloatArray, b: FloatArray): FloatArray {
            val an = normalize3(a)
            val bn = normalize3(b)
            val vx = an[1] * bn[2] - an[2] * bn[1]
            val vy = an[2] * bn[0] - an[0] * bn[2]
            val vz = an[0] * bn[1] - an[1] * bn[0]
            val c = an[0] * bn[0] + an[1] * bn[1] + an[2] * bn[2]

            if (vx * vx + vy * vy + vz * vz < 1e-6f) {
                if (c > 0f) {
                    return floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
                }
                // 180 derajat: putar terhadap sumbu tegak lurus a
                val helper = if (kotlin.math.abs(an[0]) < 0.9f) floatArrayOf(1f, 0f, 0f)
                else floatArrayOf(0f, 1f, 0f)
                val p = normalize3(
                    floatArrayOf(
                        an[1] * helper[2] - an[2] * helper[1],
                        an[2] * helper[0] - an[0] * helper[2],
                        an[0] * helper[1] - an[1] * helper[0]
                    )
                )
                return floatArrayOf(
                    2 * p[0] * p[0] - 1, 2 * p[0] * p[1], 2 * p[0] * p[2],
                    2 * p[1] * p[0], 2 * p[1] * p[1] - 1, 2 * p[1] * p[2],
                    2 * p[2] * p[0], 2 * p[2] * p[1], 2 * p[2] * p[2] - 1
                )
            }

            val k = floatArrayOf(
                0f, -vz, vy,
                vz, 0f, -vx,
                -vy, vx, 0f
            )
            val k2 = mat3Mul(k, k)
            val f = 1f / (1f + c)
            val identity = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
            val r = FloatArray(9)
            for (i in 0 until 9) r[i] = identity[i] + k[i] + k2[i] * f
            return r
        }

        private fun normalize3(v: FloatArray): FloatArray {
            val len = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
            return if (len < 1e-6f) floatArrayOf(0f, 0f, 0f)
            else floatArrayOf(v[0] / len, v[1] / len, v[2] / len)
        }

        /** Rotasi 3x3 row-major terhadap sumbu X/Y/Z (radian). */
        fun rotationMatrix(axis: Char, angle: Float): FloatArray {
            val c = kotlin.math.cos(angle)
            val s = kotlin.math.sin(angle)
            return when (axis) {
                'X' -> floatArrayOf(1f, 0f, 0f, 0f, c, -s, 0f, s, c)
                'Y' -> floatArrayOf(c, 0f, s, 0f, 1f, 0f, -s, 0f, c)
                else -> floatArrayOf(c, -s, 0f, s, c, 0f, 0f, 0f, 1f)
            }
        }
    }
}
