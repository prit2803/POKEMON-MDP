package com.example.proyek_mdp.UI.Camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide

import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.SessionManager
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.Data.remote.api.RetrofitClient
import com.example.proyek_mdp.viewmodel.CameraViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

import kotlinx.coroutines.launch

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment :
    Fragment(R.layout.fragment_camera) {

    private val pokemonTranslationMap = mapOf(
        "ピカチュウ" to "pikachu", "皮卡丘" to "pikachu",
        "フシギダネ" to "bulbasaur", "妙蛙种子" to "bulbasaur",
        "フシギソウ" to "ivysaur", "妙蛙草" to "ivysaur",
        "フシギバナ" to "venusaur", "妙蛙花" to "venusaur",
        "ヒトカゲ" to "charmander", "小火龙" to "charmander",
        "リザード" to "charmeleon", "火恐龙" to "charmeleon",
        "リザードン" to "charizard", "喷火龙" to "charizard",
        "ゼニガメ" to "squirtle", "杰尼龟" to "squirtle",
        "カメール" to "wartortle", "卡咪龟" to "wartortle",
        "カメックス" to "blastoise", "水箭龟" to "blastoise",
        "キャタピー" to "caterpie", "绿毛虫" to "caterpie",
        "トランセル" to "metapod", "铁甲蛹" to "metapod",
        "バタフリー" to "butterfree", "巴大蝶" to "butterfree",
        "ビードル" to "weedle", "独角虫" to "weedle",
        "コクーン" to "kakuna", "铁壳蛹" to "kakuna",
        "スピアー" to "beedrill", "大针蜂" to "beedrill",
        "ポッポ" to "pidgey", "波波" to "pidgey",
        "ピジョン" to "pidgeotto", "比比鸟" to "pidgeotto",
        "ピジョット" to "pidgeot", "大比鸟" to "pidgeot",
        "コラッタ" to "rattata", "小拉达" to "rattata",
        "ラッタ" to "raticate", "拉达" to "raticate",
        "オニスズメ" to "spearow", "烈雀" to "spearow",
        "オニドリル" to "fearow", "大嘴雀" to "fearow",
        "アーボ" to "ekans", "阿柏蛇" to "ekans",
        "アーボック" to "arbok", "阿柏怪" to "arbok",
        "ライチュウ" to "raichu", "雷丘" to "raichu",
        "サンド" to "sandshrew", "穿山鼠" to "sandshrew",
        "サンドパン" to "sandslash", "穿山王" to "sandslash",
        "ニドラン" to "nidoran", "尼多" to "nidoran",
        "ニドリーナ" to "nidorina", "尼多娜" to "nidorina",
        "ニドクイン" to "nidoqueen", "尼多后" to "nidoqueen",
        "ニドリーノ" to "nidorino", "尼多力诺" to "nidorino",
        "ニドキング" to "nidoking", "尼多王" to "nidoking",
        "ピッピ" to "clefairy", "皮皮" to "clefairy",
        "ピクシー" to "clefable", "皮可西" to "clefable",
        "ロコン" to "vulpix", "六尾" to "vulpix",
        "キュウコン" to "ninetales", "九尾" to "ninetales",
        "プリン" to "jigglypuff", "胖丁" to "jigglypuff",
        "プクリン" to "wigglytuff", "胖可丁" to "wigglytuff",
        "ズバット" to "zubat", "超音蝠" to "zubat",
        "ゴルバット" to "golbat", "大嘴蝠" to "golbat",
        "ナゾノクサ" to "oddish", "走路草" to "oddish",
        "クサイハナ" to "gloom", "臭臭花" to "gloom",
        "ラフレシア" to "vileplume", "霸王花" to "vileplume",
        "パラス" to "paras", "派拉斯" to "paras",
        "パラセクト" to "parasect", "派拉斯特" to "parasect",
        "コンパン" to "venonat", "毛球" to "venonat",
        "モルフォン" to "venomoth", "摩鲁蛾" to "venomoth",
        "ディグダ" to "diglett", "地鼠" to "diglett",
        "ダグトリオ" to "dugtrio", "三地鼠" to "dugtrio",
        "ニャース" to "meowth", "喵喵" to "meowth",
        "ペルシアン" to "persian", "猫老大" to "persian",
        "コダック" to "psyduck", "可达鸭" to "psyduck",
        "ゴルダック" to "golduck", "哥达鸭" to "golduck",
        "マンキー" to "mankey", "猴怪" to "mankey",
        "オコリザル" to "primeape", "火暴猴" to "primeape",
        "ガーディ" to "growlithe", "卡蒂狗" to "growlithe",
        "ウインディ" to "arcanine", "风速狗" to "arcanine",
        "ニョロモ" to "poliwag", "蚊香蝌蚪" to "poliwag",
        "ニョロゾ" to "poliwhirl", "蚊香君" to "poliwhirl",
        "ニョロボン" to "poliwrath", "蚊香泳士" to "poliwrath",
        "ケーシィ" to "abra", "凯西" to "abra",
        "ユンゲラー" to "kadabra", "勇基拉" to "kadabra",
        "フーディン" to "alakazam", "胡地" to "alakazam",
        "ワンリキー" to "machop", "腕力" to "machop",
        "ゴーリキー" to "machoke", "豪力" to "machoke",
        "カイリキー" to "machamp", "怪力" to "machamp",
        "マダツボミ" to "bellsprout", "喇叭芽" to "bellsprout",
        "ウツドン" to "weepinbell", "口呆花" to "weepinbell",
        "ウツボット" to "victreebel", "大食花" to "victreebel",
        "メノクラゲ" to "tentacool", "玛瑙水母" to "tentacool",
        "ドククラゲ" to "tentacruel", "毒刺水母" to "tentacruel",
        "イシツブテ" to "geodude", "小拳石" to "geodude",
        "ゴローン" to "graveler", "隆隆石" to "graveler",
        "ゴローニャ" to "golem", "隆隆岩" to "golem",
        "ポニータ" to "ponyta", "小火马" to "ponyta",
        "ギャロップ" to "rapidash", "烈焰马" to "rapidash",
        "ヤドン" to "slowpoke", "呆呆兽" to "slowpoke",
        "ヤドラン" to "slowbro", "呆壳兽" to "slowbro",
        "コイル" to "magnemite", "小磁怪" to "magnemite",
        "レアコイル" to "magneton", "三合一磁怪" to "magneton",
        "カモネギ" to "farfetchd", "大葱鸭" to "farfetchd",
        "ドードー" to "doduo", "嘟嘟" to "doduo",
        "ドードリオ" to "dodrio", "嘟嘟利" to "dodrio",
        "パウワウ" to "seel", "小海狮" to "seel",
        "ジュゴン" to "dewgong", "白海狮" to "dewgong",
        "ベトベター" to "grimer", "臭泥" to "grimer",
        "ベトベトン" to "muk", "臭臭泥" to "muk",
        "シェルダー" to "shellder", "大舌贝" to "shellder",
        "パルシェン" to "cloyster", "刺甲贝" to "cloyster",
        "ゴース" to "gastly", "鬼斯" to "gastly",
        "ゴースト" to "haunter", "鬼斯通" to "haunter",
        "ゲンガー" to "gengar", "耿鬼" to "gengar",
        "イワーク" to "onix", "大岩蛇" to "onix",
        "スリープ" to "drowzee", "催眠" to "drowzee",
        "スリーパー" to "hypno", "引梦貘人" to "hypno",
        "クラブ" to "krabby", "大闸蟹" to "krabby",
        "キングラー" to "kingler", "巨钳蟹" to "kingler",
        "ビリリダマ" to "voltorb", "雷电球" to "voltorb",
        "マルマイン" to "electrode", "顽皮雷弹" to "electrode",
        "タマタマ" to "exeggcute", "蛋蛋" to "exeggcute",
        "ナッシー" to "exeggutor", "椰蛋树" to "exeggutor",
        "カラカラ" to "cubone", "卡拉卡拉" to "cubone",
        "ガラガラ" to "marowak", "嘎啦嘎啦" to "marowak",
        "サワムラー" to "hitmonlee", "飞腿郎" to "hitmonlee",
        "エビワラー" to "hitmonchan", "快拳郎" to "hitmonchan",
        "ベロリンガ" to "lickitung", "大舌头" to "lickitung",
        "ドガース" to "koffing", "瓦斯弹" to "koffing",
        "マタドガス" to "weezing", "双弹瓦斯" to "weezing",
        "サイホーン" to "rhyhorn", "独角犀牛" to "rhyhorn",
        "サイドン" to "rhydon", "钻角犀兽" to "rhydon",
        "ラッキー" to "chansey", "吉利蛋" to "chansey",
        "モンジャラ" to "tangela", "藤蔓怪" to "tangela",
        "ガルーラ" to "kangaskhan", "袋兽" to "kangaskhan",
        "タッツー" to "horsea", "墨海马" to "horsea",
        "シードラ" to "seadra", "海刺龙" to "seadra",
        "トサキント" to "goldeen", "角金鱼" to "goldeen",
        "アズマオウ" to "seaking", "金鱼王" to "seaking",
        "ヒトデマン" to "staryu", "海星星" to "staryu",
        "スターミー" to "starmie", "宝石海星" to "starmie",
        "バリヤード" to "mr-mime", "魔墙人偶" to "mr-mime",
        "ストライク" to "scyther", "飞天螳螂" to "scyther",
        "ルージュラ" to "jynx", "迷唇姐" to "jynx",
        "エレブー" to "electabuzz", "电击兽" to "electabuzz",
        "ブーバー" to "magmar", "鸭嘴火兽" to "magmar",
        "カイロス" to "pinsir", "凯罗斯" to "pinsir",
        "ケンタロス" to "tauros", "肯泰罗" to "tauros",
        "コイキング" to "magikarp", "鲤鱼王" to "magikarp",
        "ギャラドス" to "gyarados", "暴鲤龙" to "gyarados",
        "ラプラス" to "lapras", "拉普拉斯" to "lapras",
        "メタモン" to "ditto", "百变怪" to "ditto",
        "イーブイ" to "eevee", "伊布" to "eevee",
        "シャワーズ" to "vaporeon", "水伊布" to "vaporeon",
        "サンダース" to "jolteon", "雷伊布" to "jolteon",
        "ブースター" to "flareon", "火伊布" to "flareon",
        "ポリゴン" to "porygon", "多边兽" to "porygon",
        "オムナイト" to "omanyte", "菊石兽" to "omanyte",
        "オムスター" to "omastar", "多刺菊石兽" to "omastar",
        "カブト" to "kabuto", "化石盔" to "kabuto",
        "カブトプス" to "kabutops", "镰刀盔" to "kabutops",
        "プテラ" to "aerodactyl", "化石翼龙" to "aerodactyl",
        "カビゴン" to "snorlax", "卡比兽" to "snorlax",
        "フリーザー" to "articuno", "急冻鸟" to "articuno",
        "サンダー" to "zapdos", "闪电鸟" to "zapdos",
        "ファイヤー" to "moltres", "火焰鸟" to "moltres",
        "ミニリュウ" to "dratini", "迷你龙" to "dratini",
        "ハクリュー" to "dragonair", "哈克龙" to "dragonair",
        "カイリュー" to "dragonite", "快龙" to "dragonite",
        "ミュウツー" to "mewtwo", "超梦" to "mewtwo",
        "ミュウ" to "mew", "梦幻" to "mew"
    )

    // ===============================
    // VIEW
    // ===============================

    private lateinit var previewView:
            PreviewView

    private lateinit var tvResult:
            TextView

    private lateinit var imgPokemon:
            ImageView

    private lateinit var btnSave:
            Button

    private lateinit var btnClear:
            Button

    // ===============================
    // CAMERA
    // ===============================

    private lateinit var cameraExecutor:
            ExecutorService

    // ===============================
    // CURRENT DATA
    // ===============================

    private var currentPokemonName =
        ""

    private var currentPokemonId =
        0 // nomor Pokedex dari API, dipakai buat speciesId (Pokedex feature)

    private var currentPokemonHp =
        0

    private var currentPokemonImage =
        ""

    // Prevent duplicate OCR spam
    private var isScanning =
        false
        
    private val viewModel: CameraViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    // ===============================
    // CAMERA PERMISSION
    // ===============================

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->

            if (granted) {

                startCamera()

            } else {

                tvResult.text =
                    "Permission kamera ditolak"
            }
        }

    // ===============================
    // LIFECYCLE
    // ===============================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        // INIT VIEW

        previewView =
            view.findViewById(
                R.id.previewView
            )

        tvResult =
            view.findViewById(
                R.id.tvResult
            )

        imgPokemon =
            view.findViewById(
                R.id.imgPokemon
            )

        btnSave =
            view.findViewById(
                R.id.btnSave
            )

        btnClear =
            view.findViewById(
                R.id.btnClear
            )

        // CAMERA EXECUTOR

        cameraExecutor =
            Executors.newSingleThreadExecutor()

        // CHECK CAMERA

        checkCameraPermission()

        // SAVE BUTTON

        btnSave.setOnClickListener {

            savePokemon()
        }

        // CLEAR BUTTON

        btnClear.setOnClickListener {

            clearPokemon()
        }
        
        // OBSERVE VIEWMODEL
        viewModel.catchSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                tvResult.text = "$currentPokemonName berhasil disimpan!"
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                tvResult.text = "Error: $errorMessage"
            }
        }
    }

    // ===============================
    // CAMERA PERMISSION
    // ===============================

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // ===============================
    // START CAMERA
    // ===============================

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            // PREVIEW

            val preview =
                Preview.Builder()
                    .build()
                    .also {

                        it.surfaceProvider =
                            previewView.surfaceProvider
                    }

            // REALTIME ANALYZER

            val imageAnalyzer =
                ImageAnalysis.Builder()

                    .setBackpressureStrategy(
                        ImageAnalysis
                            .STRATEGY_KEEP_ONLY_LATEST
                    )

                    .build()

            imageAnalyzer.setAnalyzer(
                cameraExecutor
            ) { imageProxy ->

                processImageProxy(imageProxy)
            }

            val cameraSelector =
                CameraSelector
                    .DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Log.e(
                    "CameraFragment",
                    "Camera gagal",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ===============================
    // REALTIME OCR
    // ===============================

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy
    ) {

        if (isScanning) {

            imageProxy.close()
            return
        }

        val mediaImage =
            imageProxy.image

        if (mediaImage != null) {

            val image =
                InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy
                        .imageInfo
                        .rotationDegrees
                )

            val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            latinRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d("OCR_LATIN", text)
                    val pokemonName = extractPokemonName(text)
                    if (pokemonName.isNotEmpty()) {
                        isScanning = true
                        searchPokemon(text)
                        imageProxy.close()
                    } else {
                        val japaneseRecognizer = TextRecognition.getClient(
                            JapaneseTextRecognizerOptions.Builder().build()
                        )
                        japaneseRecognizer.process(image)
                            .addOnSuccessListener { jaText ->
                                Log.d("OCR_JAPANESE", jaText.text)
                                val jaName = extractPokemonName(jaText.text)
                                if (jaName.isNotEmpty()) {
                                    isScanning = true
                                    searchPokemon(jaText.text)
                                    imageProxy.close()
                                } else {
                                    val chineseRecognizer = TextRecognition.getClient(
                                        ChineseTextRecognizerOptions.Builder().build()
                                    )
                                    chineseRecognizer.process(image)
                                        .addOnSuccessListener { zhText ->
                                            Log.d("OCR_CHINESE", zhText.text)
                                            val zhName = extractPokemonName(zhText.text)
                                            if (zhName.isNotEmpty()) {
                                                isScanning = true
                                                searchPokemon(zhText.text)
                                            } else {
                                                tvResult.text = "Pokemon tidak terdeteksi"
                                            }
                                            imageProxy.close()
                                        }
                                        .addOnFailureListener {
                                            tvResult.text = "OCR gagal"
                                            isScanning = false
                                            imageProxy.close()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                tvResult.text = "OCR gagal"
                                isScanning = false
                                imageProxy.close()
                            }
                    }
                }
                .addOnFailureListener {
                    tvResult.text = "OCR gagal"
                    isScanning = false
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    // ===============================
    // EXTRACT POKEMON NAME
    // ===============================

    private fun extractPokemonName(
        text: String
    ): String {
        val lowerText = text.lowercase()
        for ((key, englishName) in pokemonTranslationMap) {
            if (lowerText.contains(key.lowercase())) {
                return englishName
            }
        }

        val lines =
            text.lines()

        for (line in lines) {

            val cleaned =
                line
                    .trim()
                    .split(" ")
                    .firstOrNull()
                    ?.lowercase()
                    ?.replace(
                        "[^a-z]".toRegex(),
                        ""
                    )

            if (
                !cleaned.isNullOrEmpty()
            ) {

                return cleaned
            }
        }

        return ""
    }

    // ===============================
    // EXTRACT HP
    // ===============================

    private fun extractPokemonHp(
        text: String
    ): Int {

        val regex =
            Regex("\\b\\d{2,3}\\b")

        val matches =
            regex.findAll(text)

        for (match in matches) {

            val value =
                match.value.toIntOrNull()

            if (
                value != null &&
                value in 40..350
            ) {

                return value
            }
        }

        return 0
    }

    // ===============================
    // SEARCH POKEMON
    // ===============================

    private fun searchPokemon(
        text: String
    ) {

        val pokemonName =
            extractPokemonName(text)

        currentPokemonHp =
            extractPokemonHp(text)

        if (
            pokemonName.isEmpty()
        ) {

            tvResult.text =
                "Pokemon tidak ditemukan"

            isScanning = false

            return
        }

        lifecycleScope.launch {

            try {

                val response =
                    RetrofitClient.api
                        .getPokemon(
                            pokemonName
                        )

                if (response.isSuccessful) {

                    val pokemon =
                        response.body()

                    currentPokemonName =
                        pokemon?.name ?: ""

                    currentPokemonId =
                        pokemon?.id ?: 0

                    currentPokemonImage =
                        pokemon?.sprites
                            ?.front_default
                            ?: ""

                    val pokemonType =
                        pokemon?.types
                            ?.firstOrNull()
                            ?.type
                            ?.name
                            ?: "-"

                    val pokemonHeight =
                        pokemon?.height ?: 0

                    val pokemonWeight =
                        pokemon?.weight ?: 0

                    tvResult.text =
                        """
                        Pokemon:
                        ${pokemon?.name}

                        Type:
                        $pokemonType

                        HP:
                        $currentPokemonHp

                        Height:
                        $pokemonHeight

                        Weight:
                        $pokemonWeight
                        """.trimIndent()

                    Glide.with(requireContext())
                        .load(
                            pokemon?.sprites
                                ?.front_default
                        )
                        .into(imgPokemon)

                } else {

                    tvResult.text =
                        "Pokemon tidak ditemukan"

                    isScanning = false
                }

            } catch (e: Exception) {

                tvResult.text =
                    "Error: ${e.message}"

                isScanning = false
            }
        }
    }

    // ===============================
    // SAVE DATABASE
    // ===============================

    private fun savePokemon() {

        if (currentPokemonName.isEmpty()) {

            tvResult.text = "Belum ada pokemon"
            return
        }

        val userId = SessionManager(requireContext()).getUserId()

        if (userId == -1) {

            Toast.makeText(
                requireContext(),
                "Sesi login gak ketemu, coba login ulang",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        viewModel.catchPokemon(
            userId = userId,
            speciesId = currentPokemonId,
            name = currentPokemonName,
            hp = currentPokemonHp,
            imageUrl = currentPokemonImage
        )
    }

    // ===============================
    // CLEAR POKEMON
    // ===============================

    private fun clearPokemon() {

        currentPokemonName = ""

        currentPokemonId = 0

        currentPokemonHp = 0

        currentPokemonImage = ""

        tvResult.text =
            "Arahkan kartu Pokémon"

        imgPokemon.setImageDrawable(null)

        isScanning = false
    }

    // ===============================
    // DESTROY
    // ===============================

    override fun onDestroyView() {

        super.onDestroyView()

        cameraExecutor.shutdown()
    }
}