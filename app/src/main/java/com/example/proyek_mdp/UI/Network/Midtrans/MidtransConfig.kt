package com.example.proyek_mdp.UI.Network.Midtrans

/**
 * GANTI dua nilai di bawah ini dengan Server Key & Client Key SANDBOX kamu
 * sendiri, ambil dari: https://dashboard.sandbox.midtrans.com/settings/config_info
 *
 * PENTING - JANGAN PERNAH commit Server Key asli ke GitHub public repo!
 * Untuk tugas kuliah ini oke-oke aja ditaruh langsung di kode (soalnya SANDBOX,
 * bukan uang asli), tapi kalau nanti bikin app production sungguhan, Server Key
 * WAJIB cuma ada di backend server, gak boleh ada di dalam APK sama sekali.
 */
object MidtransConfig {
    const val SERVER_KEY = "SB-Mid-server-ZjQWXuk8HxJnrzY9f5Ht5_HZ"
    const val CLIENT_KEY = "SB-Mid-client-yvv0oP_RGqdDOZAa"

    // Base URL Sandbox. Kalau nanti udah siap ke production, ganti ke:
    // https://app.midtrans.com/snap/v1/  dan  https://api.midtrans.com/v2/
    const val SNAP_BASE_URL = "https://app.sandbox.midtrans.com/snap/v1/"
    const val CORE_API_BASE_URL = "https://api.sandbox.midtrans.com/v2/"

    // 1 koin = Rp100. Jadi 100 koin = Rp10.000, dst. Bebas diubah sesuai kebutuhan.
    const val RUPIAH_PER_COIN = 100
}