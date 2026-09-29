import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
    getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import {
    getFirestore, collection, getDocs, doc, setDoc, updateDoc, deleteDoc
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

// =========================================================
// GANTI dengan config Web App dari Firebase Console:
// Project settings -> General -> Your apps -> Web app -> Config
// =========================================================
const firebaseConfig = {
  apiKey: "AIzaSyDqCrjIX-pmA_YT3OJbX4TK1_UMiG6zxVk",
  authDomain: "pokemon-mdp.firebaseapp.com",
  projectId: "pokemon-mdp",
  storageBucket: "pokemon-mdp.firebasestorage.app",
  messagingSenderId: "890924297741",
  appId: "1:890924297741:web:6cbd33cc87b7c9a1b5ef44",
  measurementId: "G-SP6Q16SKLM"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// =========================================================
// STATE
// =========================================================
let usersCache = [];
let postsCache = [];
let pokemonCache = [];
let modalSaveHandler = null;

// =========================================================
// HELPERS
// =========================================================
const $ = (id) => document.getElementById(id);

function toast(msg) {
    const t = $("toast");
    t.textContent = msg;
    t.classList.remove("hidden");
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.classList.add("hidden"), 2500);
}

function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, (c) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[c]));
}

function openModal(title, bodyHtml, onSave) {
    $("modalTitle").textContent = title;
    $("modalBody").innerHTML = bodyHtml;
    modalSaveHandler = onSave;
    $("modal").classList.remove("hidden");
}

function closeModal() {
    $("modal").classList.add("hidden");
    modalSaveHandler = null;
}

// =========================================================
// AUTH
// =========================================================
$("loginBtn").onclick = async () => {
    $("loginError").textContent = "";
    try {
        await signInWithEmailAndPassword(auth, $("loginEmail").value, $("loginPassword").value);
    } catch (e) {
        $("loginError").textContent = "Gagal login: " + e.message;
    }
};

$("logoutBtn").onclick = () => signOut(auth);

onAuthStateChanged(auth, (user) => {
    if (user) {
        $("loginScreen").classList.add("hidden");
        $("appScreen").classList.remove("hidden");
        loadAll();
    } else {
        $("appScreen").classList.add("hidden");
        $("loginScreen").classList.remove("hidden");
    }
});

// =========================================================
// NAV
// =========================================================
document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.onclick = () => {
        document.querySelectorAll(".nav-btn").forEach((b) => b.classList.remove("active"));
        document.querySelectorAll(".section").forEach((s) => s.classList.remove("active"));
        btn.classList.add("active");
        $("section-" + btn.dataset.section).classList.add("active");
    };
});

// =========================================================
// LOAD DATA
// =========================================================
async function loadAll() {
    try {
        const [uSnap, pSnap, kSnap, paySnap] = await Promise.all([
            getDocs(collection(db, "users")),
            getDocs(collection(db, "posts")),
            getDocs(collection(db, "pokemon")),
            getDocs(collection(db, "payment_history"))
        ]);

        usersCache = uSnap.docs.map((d) => ({ _docId: d.id, ...d.data() }));
        postsCache = pSnap.docs.map((d) => ({ _docId: d.id, ...d.data() }));
        pokemonCache = kSnap.docs.map((d) => ({ _docId: d.id, ...d.data() }));
        const payments = paySnap.docs.map((d) => d.data());

        renderUsers();
        renderPosts();
        renderPokemon();
        renderPayments(payments);

        $("statUsers").textContent = usersCache.length;
        $("statPosts").textContent = postsCache.length;
        $("statPokemon").textContent = pokemonCache.length;
        $("statBanned").textContent = usersCache.filter((u) => Number(u.isBanned) === 1).length;
    } catch (e) {
        toast("Gagal memuat data: " + e.message);
    }
}

// =========================================================
// USERS
// =========================================================
function renderUsers() {
    const q = ($("userSearch").value || "").toLowerCase();
    const rows = usersCache
        .filter((u) => !q || (u.username || "").toLowerCase().includes(q) || (u.email || "").toLowerCase().includes(q))
        .map((u) => `
            <tr>
                <td>${escapeHtml(u.username)}</td>
                <td>${escapeHtml(u.email)}</td>
                <td>${escapeHtml(u.coins ?? 0)}</td>
                <td>${Number(u.isBanned) === 1 ? '<span class="badge bad">Banned</span>' : '<span class="badge ok">Aktif</span>'}</td>
                <td>
                    <button class="small btn-edit" data-act="edit-user" data-doc="${u._docId}">Edit</button>
                    <button class="small btn-give" data-act="give" data-doc="${u._docId}">Beri Pokemon</button>
                    <button class="small btn-ban" data-act="ban" data-doc="${u._docId}">${Number(u.isBanned) === 1 ? "Unban" : "Ban"}</button>
                    <button class="small btn-del" data-act="del-user" data-doc="${u._docId}">Hapus</button>
                </td>
            </tr>`).join("");
    $("usersTable").querySelector("tbody").innerHTML = rows;
}

$("userSearch").oninput = renderUsers;

// =========================================================
// POSTS
// =========================================================
function renderPosts() {
    const rows = postsCache.map((p) => `
        <tr>
            <td>${escapeHtml(p.title)}</td>
            <td>${escapeHtml(p.category)}</td>
            <td>${escapeHtml(p.price)}</td>
            <td>${escapeHtml(p.stock ?? 0)}</td>
            <td>${Number(p.isActive) === 1 ? '<span class="badge ok">Aktif</span>' : '<span class="badge bad">Nonaktif</span>'}</td>
            <td>
                <button class="small btn-edit" data-act="edit-post" data-doc="${p._docId}">Edit</button>
                <button class="small btn-ban" data-act="toggle-post" data-doc="${p._docId}">${Number(p.isActive) === 1 ? "Matikan" : "Aktifkan"}</button>
                <button class="small btn-del" data-act="del-post" data-doc="${p._docId}">Hapus</button>
            </td>
        </tr>`).join("");
    $("postsTable").querySelector("tbody").innerHTML = rows;
}

function postForm(p = {}) {
    return `
        <input id="f_title" placeholder="Judul" value="${escapeHtml(p.title || "")}" />
        <input id="f_desc" placeholder="Deskripsi" value="${escapeHtml(p.description || "")}" />
        <input id="f_price" type="number" placeholder="Harga" value="${p.price ?? 0}" />
        <input id="f_category" placeholder="Kategori" value="${escapeHtml(p.category || "")}" />
        <input id="f_stock" type="number" placeholder="Stok" value="${p.stock ?? 0}" />
        <input id="f_image" placeholder="URL gambar (opsional)" value="${escapeHtml(p.imagePath || "")}" />
    `;
}

$("addPostBtn").onclick = () => {
    openModal("Tambah Post", postForm(), async () => {
        const id = Date.now();
        await setDoc(doc(db, "posts", String(id)), {
            id,
            title: $("f_title").value,
            description: $("f_desc").value,
            price: Number($("f_price").value),
            category: $("f_category").value,
            imagePath: $("f_image").value || null,
            isActive: 1,
            stock: Number($("f_stock").value),
            createdAt: Date.now()
        });
        toast("Post ditambahkan");
        closeModal();
        loadAll();
    });
};

// =========================================================
// POKEMON
// =========================================================
function renderPokemon() {
    const q = ($("pokemonSearch").value || "").toLowerCase();
    const rows = pokemonCache
        .filter((k) => !q || (k.name || "").toLowerCase().includes(q) || String(k.userId).includes(q))
        .map((k) => `
            <tr>
                <td>${escapeHtml(k.name)}</td>
                <td>${escapeHtml(k.userId)}</td>
                <td>${escapeHtml(k.level ?? 1)}</td>
                <td>${escapeHtml(k.hp ?? 0)}</td>
                <td>${escapeHtml(k.speciesId ?? 0)}</td>
                <td>
                    <button class="small btn-edit" data-act="edit-pokemon" data-doc="${k._docId}">Edit</button>
                    <button class="small btn-del" data-act="del-pokemon" data-doc="${k._docId}">Hapus</button>
                </td>
            </tr>`).join("");
    $("pokemonTable").querySelector("tbody").innerHTML = rows;
}

$("pokemonSearch").oninput = renderPokemon;

function givePokemonForm() {
    const userOptions = usersCache
        .map((u) => `<option value="${escapeHtml(u._docId)}">${escapeHtml(u.username)}</option>`).join("");
    return `
        <select id="g_user">${userOptions}</select>
        <input id="g_query" placeholder="Nama / nomor Pokemon (mis. charizard / 6)" />
        <input id="g_level" type="number" placeholder="Level" value="1" />
        <input id="g_hp" type="number" placeholder="HP" value="50" />
    `;
}

$("givePokemonBtn").onclick = () => {
    if (usersCache.length === 0) { toast("Belum ada user"); return; }
    openModal("Beri / Tambah Pokemon", givePokemonForm(), async () => {
        await givePokemonTo($("g_user").value, $("g_query").value, $("g_level").value, $("g_hp").value);
    });
};

// =========================================================
// PAYMENTS
// =========================================================
function renderPayments(payments) {
    const rows = payments.map((p) => `
        <tr>
            <td>${escapeHtml(p.userId)}</td>
            <td>${escapeHtml(p.paymentMethod)}</td>
            <td>${escapeHtml(p.coinAmount)}</td>
            <td>${escapeHtml(p.totalPrice)}</td>
            <td>${escapeHtml(p.status)}</td>
            <td>${p.transactionDate ? new Date(p.transactionDate).toLocaleString() : "-"}</td>
        </tr>`).join("");
    $("paymentsTable").querySelector("tbody").innerHTML = rows;
}

// =========================================================
// TABLE ACTIONS
// =========================================================
document.body.addEventListener("click", async (e) => {
    const btn = e.target.closest("button[data-act]");
    if (!btn) return;
    const act = btn.dataset.act;
    const docId = btn.dataset.doc;

    try {
        if (act === "ban") {
            const u = usersCache.find((x) => x._docId === docId);
            const newStatus = Number(u.isBanned) === 1 ? 0 : 1;
            await updateDoc(doc(db, "users", docId), { isBanned: newStatus });
            toast("Status user diubah");
            loadAll();
        } else if (act === "del-user") {
            if (!confirm("Hapus user ini?")) return;
            await deleteDoc(doc(db, "users", docId));
            toast("User dihapus");
            loadAll();
        } else if (act === "edit-user") {
            const u = usersCache.find((x) => x._docId === docId);
            openModal("Edit User", `
                <input id="eu_username" placeholder="Username" value="${escapeHtml(u.username)}" />
                <input id="eu_email" placeholder="Email" value="${escapeHtml(u.email)}" />
                <input id="eu_coins" type="number" placeholder="Coins" value="${u.coins ?? 0}" />
            `, async () => {
                await updateDoc(doc(db, "users", docId), {
                    username: $("eu_username").value,
                    email: $("eu_email").value,
                    coins: Number($("eu_coins").value)
                });
                toast("User diperbarui");
                closeModal();
                loadAll();
            });
        } else if (act === "give") {
            openModal("Beri / Tambah Pokemon", givePokemonForm(), async () => {
                await givePokemonTo($("g_user").value, $("g_query").value, $("g_level").value, $("g_hp").value);
            });
            // pre-select user
            $("g_user").value = docId;
        } else if (act === "edit-post") {
            const p = postsCache.find((x) => x._docId === docId);
            openModal("Edit Post", postForm(p), async () => {
                await updateDoc(doc(db, "posts", docId), {
                    title: $("f_title").value,
                    description: $("f_desc").value,
                    price: Number($("f_price").value),
                    category: $("f_category").value,
                    imagePath: $("f_image").value || null,
                    stock: Number($("f_stock").value)
                });
                toast("Post diperbarui");
                closeModal();
                loadAll();
            });
        } else if (act === "toggle-post") {
            const p = postsCache.find((x) => x._docId === docId);
            await updateDoc(doc(db, "posts", docId), { isActive: Number(p.isActive) === 1 ? 0 : 1 });
            toast("Status post diubah");
            loadAll();
        } else if (act === "del-post") {
            if (!confirm("Hapus post ini?")) return;
            await deleteDoc(doc(db, "posts", docId));
            toast("Post dihapus");
            loadAll();
        } else if (act === "edit-pokemon") {
            const k = pokemonCache.find((x) => x._docId === docId);
            openModal("Edit Pokemon", `
                <input id="pk_name" placeholder="Nama" value="${escapeHtml(k.name)}" />
                <input id="pk_level" type="number" placeholder="Level" value="${k.level ?? 1}" />
                <input id="pk_hp" type="number" placeholder="HP" value="${k.hp ?? 0}" />
                <input id="pk_locked" type="number" placeholder="isLocked (0/1)" value="${k.isLocked ?? 0}" />
            `, async () => {
                await updateDoc(doc(db, "pokemon", docId), {
                    name: $("pk_name").value,
                    level: Number($("pk_level").value),
                    hp: Number($("pk_hp").value),
                    isLocked: Number($("pk_locked").value)
                });
                toast("Pokemon diperbarui");
                closeModal();
                loadAll();
            });
        } else if (act === "del-pokemon") {
            if (!confirm("Hapus Pokemon ini?")) return;
            await deleteDoc(doc(db, "pokemon", docId));
            toast("Pokemon dihapus");
            loadAll();
        }
    } catch (err) {
        toast("Gagal: " + err.message);
    }
});

async function givePokemonTo(userId, queryRaw, level, hp) {
    const query = (queryRaw || "").trim().toLowerCase();
    if (!query) { toast("Isi nama/nomor Pokemon"); return; }

    const res = await fetch(`https://pokeapi.co/api/v2/pokemon/${query}`);
    if (!res.ok) { toast("Pokemon tidak ditemukan"); return; }
    const body = await res.json();

    const id = Date.now();
    await setDoc(doc(db, "pokemon", String(id)), {
        id,
        userId: Number(userId),
        speciesId: body.id,
        name: body.name.charAt(0).toUpperCase() + body.name.slice(1),
        hp: Number(hp) || 50,
        imageUrl: body.sprites?.front_default || "",
        level: Number(level) || 1,
        exp: 0,
        isStarter: 0,
        isLocked: 0,
        caughtAt: Date.now()
    });
    toast("Pokemon diberikan");
    closeModal();
    loadAll();
}

// =========================================================
// MODAL
// =========================================================
$("modalCancel").onclick = closeModal;
$("modalSave").onclick = async () => {
    if (!modalSaveHandler) return;
    try {
        await modalSaveHandler();
    } catch (e) {
        toast("Gagal: " + e.message);
    }
};
