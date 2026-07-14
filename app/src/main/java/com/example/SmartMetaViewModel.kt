package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Models
data class Officer(
    val id: String,
    val name: String,
    val role: String,
    val latitude: Double,
    val longitude: Double,
    val status: String, // "Aktif", "Patroli", "Istirahat"
    val avatarUrl: String = ""
)

data class WorkReport(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val imageUrl: String = "",
    val reporter: String,
    val isFromCitizen: Boolean = false,
    val status: String = "Selesai" // "Selesai", "Diproses"
)

data class ChatMessage(
    val id: String,
    val sender: String,
    val receiver: String,
    val text: String,
    val timestamp: String,
    val isFromOfficer: Boolean
)

data class WasteReward(
    val id: String,
    val name: String,
    val pointsNeeded: Int,
    val description: String,
    val category: String // "Sembako", "Pajak", "Lainnya"
)

enum class Screen {
    Login,
    PetugasDashboard,
    WargaDashboard,
    AdminSettings
}

class SmartMetaViewModel(application: Application) : AndroidViewModel(application) {

    // Authentication & Navigation
    private val _currentScreen = MutableStateFlow(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _loggedInUser = MutableStateFlow<String?>(null)
    val loggedInUser: StateFlow<String?> = _loggedInUser.asStateFlow()

    private val _loggedInRole = MutableStateFlow<String?>(null) // "Petugas" or "Warga"
    val loggedInRole: StateFlow<String?> = _loggedInRole.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Working Hours Setting (can be configured in admin / dashboard)
    private val _workingHours = MutableStateFlow("08:00 - 17:00")
    val workingHours: StateFlow<String> = _workingHours.asStateFlow()

    // Waste Bank Exchange rate setting
    private val _pointPerKg = MutableStateFlow(100)
    val pointPerKg: StateFlow<Int> = _pointPerKg.asStateFlow()

    // Attendance State
    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _clockInTime = MutableStateFlow<String?>(null)
    val clockInTime: StateFlow<String?> = _clockInTime.asStateFlow()

    private val _clockOutTime = MutableStateFlow<String?>(null)
    val clockOutTime: StateFlow<String?> = _clockOutTime.asStateFlow()

    // Citizen Point Accumulation
    private val _citizenPoints = MutableStateFlow(1250) // Starting points for demo
    val citizenPoints: StateFlow<Int> = _citizenPoints.asStateFlow()

    // List of Officers (for mapping & chat)
    private val _officers = MutableStateFlow<List<Officer>>(emptyList())
    val officers: StateFlow<List<Officer>> = _officers.asStateFlow()

    // List of Work & Citizen Reports
    private val _reports = MutableStateFlow<List<WorkReport>>(emptyList())
    val reports: StateFlow<List<WorkReport>> = _reports.asStateFlow()

    // Chat History
    private val _chats = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chats: StateFlow<List<ChatMessage>> = _chats.asStateFlow()

    // Waste Bank Rewards List
    private val _rewards = MutableStateFlow<List<WasteReward>>(emptyList())
    val rewards: StateFlow<List<WasteReward>> = _rewards.asStateFlow()

    // Notification list to show logs
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        // Pre-populate Officers
        _officers.value = listOf(
            Officer("O1", "Budi Santoso", "Petugas Kebersihan", -6.2058, 106.8436, "Aktif"),
            Officer("O2", "Rahmat Hidayat", "Petugas Patroli Sampah", -6.2118, 106.8486, "Patroli"),
            Officer("O3", "Siti Aminah", "Petugas Administrasi", -6.2088, 106.8416, "Istirahat")
        )

        // Pre-populate Reports
        _reports.value = listOf(
            WorkReport(
                "R1",
                "Pembersihan Saluran Air Sudirman",
                "Saluran air tersumbat sampah plastik di depan halte busway Sudirman telah dibersihkan.",
                "14 Juli 2026 08:30",
                "",
                "Budi Santoso",
                isFromCitizen = false
            ),
            WorkReport(
                "R2",
                "Tumpukan Sampah Liar Menteng",
                "Ada tumpukan sampah plastik dan rumah tangga yang berbau tidak sedap di trotoar jalan.",
                "14 Juli 2026 09:00",
                "",
                "Andi (Warga)",
                isFromCitizen = true,
                status = "Diproses"
            ),
            WorkReport(
                "R3",
                "Pilah Sampah Plastik Pos 1",
                "Merapikan dan menimbang hasil setoran plastik di Bank Sampah pusat.",
                "13 Juli 2026 15:45",
                "",
                "Siti Aminah",
                isFromCitizen = false
            )
        )

        // Pre-populate Chats
        _chats.value = listOf(
            ChatMessage("M1", "testwarga", "Budi Santoso", "Halo Pak Budi, apakah hari ini ada penjemputan sampah plastik di wilayah RT 05?", "09:05 AM", false),
            ChatMessage("M2", "Budi Santoso", "testwarga", "Halo! Ya betul, jadwal armada kami akan lewat sekitar jam 10:30 ya Bu.", "09:07 AM", true),
            ChatMessage("M3", "testpetugas", "Admin SMARTMETA", "Pak Admin, timbangan digital di Bank Sampah RT 02 memerlukan kalibrasi ulang.", "08:15 AM", true),
            ChatMessage("M4", "Admin SMARTMETA", "testpetugas", "Baik Pak Budi, tim teknis kami akan meluncur siang ini untuk kalibrasi.", "08:20 AM", false)
        )

        // Pre-populate Rewards
        _rewards.value = listOf(
            WasteReward("W1", "Paket Sembako Premium (Beras 5kg + Minyak 1L)", 500, "Tukarkan 500 poin untuk paket sembako lengkap guna membantu kebutuhan pokok Anda.", "Sembako"),
            WasteReward("W2", "Pembayaran Pajak PBB (Subsidi Rp 50.000)", 400, "Gunakan 400 poin untuk memotong tagihan Pajak Bumi & Bangunan Anda.", "Pajak"),
            WasteReward("W3", "Voucher Token Listrik PLN Rp 25.000", 250, "Tukarkan 250 poin dengan token listrik PLN prabayar senilai Rp 25.000.", "Pajak"),
            WasteReward("W4", "Minyak Goreng Sunco 2 Liter", 200, "Minyak goreng jernih kualitas tinggi 2L.", "Sembako"),
            WasteReward("W5", "Beras Pandan Wangi 2kg", 180, "Beras pandan wangi pulen asli pertanian lokal.", "Sembako")
        )

        _logs.value = listOf(
            "Sistem SMARTMETA diinisialisasi.",
            "Tersedia 3 petugas terdekat dari lokasi Anda."
        )
    }

    // Authentication Actions
    fun login(user: String, pass: String): Boolean {
        _authError.value = null
        if (user == "testpetugas" && pass == "test") {
            _loggedInUser.value = "testpetugas"
            _loggedInRole.value = "Petugas"
            _currentScreen.value = Screen.PetugasDashboard
            addLog("Petugas berhasil login: testpetugas")
            return true
        } else if (user == "testwarga" && pass == "test") {
            _loggedInUser.value = "testwarga"
            _loggedInRole.value = "Warga"
            _currentScreen.value = Screen.WargaDashboard
            addLog("Warga berhasil login: testwarga")
            return true
        } else {
            _authError.value = "Username atau Password salah!"
            return false
        }
    }

    fun logout() {
        _loggedInUser.value = null
        _loggedInRole.value = null
        _currentScreen.value = Screen.Login
        addLog("Pengguna log out.")
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Dashboard Configurations
    fun setWorkingHours(hours: String) {
        _workingHours.value = hours
        addLog("Jam kerja diperbarui menjadi: $hours")
    }

    fun setPointPerKg(rate: Int) {
        _pointPerKg.value = rate
        addLog("Kurs poin Bank Sampah diperbarui menjadi: $rate Poin/Kg")
    }

    // Attendance (Absensi) Actions
    fun clockIn() {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _isClockedIn.value = true
        _clockInTime.value = currentTime
        _clockOutTime.value = null
        addLog("Petugas melakukan Absen Masuk pada pukul $currentTime")
    }

    fun clockOut() {
        if (!_isClockedIn.value) return
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _isClockedIn.value = false
        _clockOutTime.value = currentTime
        addLog("Petugas melakukan Absen Keluar pada pukul $currentTime")
    }

    // Reports Actions
    fun addWorkReport(title: String, desc: String, fromCitizen: Boolean, reporterName: String, status: String = "Selesai") {
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
        val newReport = WorkReport(
            id = "R${_reports.value.size + 1}",
            title = title,
            description = desc,
            timestamp = currentTime,
            imageUrl = "", // empty represents simulated attachment
            reporter = reporterName,
            isFromCitizen = fromCitizen,
            status = status
        )
        _reports.value = listOf(newReport) + _reports.value
        addLog("Laporan baru berhasil dikirim: \"$title\" oleh $reporterName")
    }

    // Chat Actions
    fun sendChatMessage(senderName: String, receiverName: String, text: String, isFromOfficer: Boolean) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newMessage = ChatMessage(
            id = "M${_chats.value.size + 1}",
            sender = senderName,
            receiver = receiverName,
            text = text,
            timestamp = currentTime,
            isFromOfficer = isFromOfficer
        )
        _chats.value = _chats.value + newMessage
    }

    // Waste Bank Actions
    fun earnPoints(kg: Double) {
        val earned = (kg * _pointPerKg.value).toInt()
        _citizenPoints.value += earned
        addLog("Setoran sampah berhasil! Menimbang ${kg}kg. Anda mendapatkan $earned poin.")
    }

    fun redeemReward(reward: WasteReward): Boolean {
        if (_citizenPoints.value >= reward.pointsNeeded) {
            _citizenPoints.value -= reward.pointsNeeded
            addLog("Berhasil menukar ${reward.pointsNeeded} poin dengan: ${reward.name}")
            return true
        }
        return false
    }

    fun transferPoints(recipient: String, amount: Int): Boolean {
        if (amount <= 0 || _citizenPoints.value < amount) return false
        _citizenPoints.value -= amount
        addLog("Berhasil transfer $amount poin ke sesama warga: $recipient")
        return true
    }

    private fun addLog(message: String) {
        _logs.value = listOf(message) + _logs.value
    }
}
