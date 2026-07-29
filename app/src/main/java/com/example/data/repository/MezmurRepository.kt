package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.MezmurDao
import com.example.data.local.MezmurEntity
import com.example.data.local.SearchHistoryEntity
import com.example.util.GeezUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MezmurRepository(
    private val mezmurDao: MezmurDao,
    private val context: Context
) {

    private val _syncStatus = MutableStateFlow("SYNC: READY (OFFLINE CACHE ACTIVE)")
    val syncStatus: StateFlow<String> = _syncStatus

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    val allMezmurs: Flow<List<MezmurEntity>> = mezmurDao.getAllMezmurs()
    val favoriteMezmurs: Flow<List<MezmurEntity>> = mezmurDao.getFavoriteMezmurs()
    val recentSearchHistory: Flow<List<SearchHistoryEntity>> = mezmurDao.getRecentSearchHistory()

    suspend fun insertSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            mezmurDao.insertSearchQuery(SearchHistoryEntity(searchQuery = trimmed, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        mezmurDao.deleteSearchQuery(query)
    }

    suspend fun clearSearchHistory() {
        mezmurDao.clearSearchHistory()
    }

    init {
        // Initialize local cache by reading from assets/lyrics/*.txt folder
        CoroutineScope(Dispatchers.IO).launch {
            syncFromAssetsFolder()
            updateSyncStats()
            setupFirestoreListener()
        }
    }

    suspend fun syncFromAssetsFolder() {
        try {
            val existingMap = mezmurDao.getAllMezmursList().associateBy { it.id }
            val assetManager = context.assets
            val entities = mutableListOf<MezmurEntity>()

            // 1. Read lyrics folder
            val lyricsFiles = assetManager.list("lyrics")?.filter { it.endsWith(".txt", ignoreCase = true) }?.sorted() ?: emptyList()
            lyricsFiles.forEachIndexed { index, fileName ->
                val title = fileName.removeSuffix(".txt").removeSuffix(".TXT")
                val content = try {
                    assetManager.open("lyrics/$fileName").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    "ግጥም ማግኘት አልተቻለም"
                }
                val number = index + 1
                val id = "asset_$fileName"
                val existing = existingMap[id]

                entities.add(
                    MezmurEntity(
                        id = id,
                        title = title,
                        artist = "የመዝሙር ደብተር",
                        category = "መዝሙር",
                        lyrics = content,
                        numberGeez = GeezUtil.toGeezNumeral(number),
                        numberInt = number,
                        isFavorite = existing?.isFavorite ?: false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // 2. Read wudase/amharic folder
            val wudaseAmharicFiles = assetManager.list("wudase/amharic")?.filter { it.endsWith(".txt", ignoreCase = true) }?.sorted() ?: emptyList()
            wudaseAmharicFiles.forEachIndexed { index, fileName ->
                val title = fileName.removeSuffix(".txt").removeSuffix(".TXT")
                val content = try {
                    assetManager.open("wudase/amharic/$fileName").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    "ጸሎቱን ማንበብ አልተቻለም"
                }
                val number = index + 1
                val id = "wudase_amharic_$fileName"
                val existing = existingMap[id]

                entities.add(
                    MezmurEntity(
                        id = id,
                        title = title,
                        artist = "ውዳሴ ማርያም (አማርኛ)",
                        category = "ውዳሴ ማርያም",
                        lyrics = content,
                        numberGeez = GeezUtil.toGeezNumeral(number),
                        numberInt = number,
                        isFavorite = existing?.isFavorite ?: false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // 3. Read wudase/geez folder
            val wudaseGeezFiles = assetManager.list("wudase/geez")?.filter { it.endsWith(".txt", ignoreCase = true) }?.sorted() ?: emptyList()
            wudaseGeezFiles.forEachIndexed { index, fileName ->
                val title = fileName.removeSuffix(".txt").removeSuffix(".TXT")
                val content = try {
                    assetManager.open("wudase/geez/$fileName").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    "ጸሎቱን ማንበብ አልተቻለም"
                }
                val number = index + 1
                val id = "wudase_geez_$fileName"
                val existing = existingMap[id]

                entities.add(
                    MezmurEntity(
                        id = id,
                        title = title,
                        artist = "ውዳሴ ማርያም (ግዕዝ)",
                        category = "ውዳሴ ማርያም",
                        lyrics = content,
                        numberGeez = GeezUtil.toGeezNumeral(number),
                        numberInt = number,
                        isFavorite = existing?.isFavorite ?: false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // 4. Read welaytgna folder
            val welaytgnaFiles = assetManager.list("welaytgna")?.filter { it.endsWith(".txt", ignoreCase = true) }?.sorted() ?: emptyList()
            welaytgnaFiles.forEachIndexed { index, fileName ->
                val title = fileName.removeSuffix(".txt").removeSuffix(".TXT")
                val content = try {
                    assetManager.open("welaytgna/$fileName").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    "ግጥም ማግኘት አልተቻለም"
                }
                val number = index + 1
                val id = "welaytgna_$fileName"
                val existing = existingMap[id]

                entities.add(
                    MezmurEntity(
                        id = id,
                        title = title,
                        artist = "የወላይትኛ መዝሙር",
                        category = "ወላይትኛ",
                        lyrics = content,
                        numberGeez = GeezUtil.toGeezNumeral(number),
                        numberInt = number,
                        isFavorite = existing?.isFavorite ?: false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            if (entities.isNotEmpty()) {
                mezmurDao.insertAll(entities)
                
                // Identify and delete any old asset-based entries that are no longer in the assets folder
                val newIds = entities.map { it.id }.toSet()
                val toDelete = existingMap.keys.filter { id ->
                    (id.startsWith("asset_") || id.startsWith("wudase_amharic_") || id.startsWith("wudase_geez_") || id.startsWith("welaytgna_")) && !newIds.contains(id)
                }
                toDelete.forEach { id ->
                    mezmurDao.deleteById(id)
                }
                Log.d("MezmurRepo", "Successfully loaded ${entities.size} items from assets and cleaned up ${toDelete.size} removed items")
            } else if (mezmurDao.getCount() == 0) {
                seedInitialData()
            }
        } catch (e: Exception) {
            Log.e("MezmurRepo", "Error syncing assets: ${e.message}")
            if (mezmurDao.getCount() == 0) {
                seedInitialData()
            }
        }
    }

    suspend fun updateSyncStats() {
        val count = mezmurDao.getCount()
        _syncStatus.value = "SYNC: $count LYRICS CACHED • DATABASE READY"
    }

    fun getMezmursByCategory(category: String): Flow<List<MezmurEntity>> {
        return if (category == "ሁሉም" || category.isEmpty()) {
            mezmurDao.getAllMezmurs()
        } else {
            mezmurDao.getMezmursByCategory(category)
        }
    }

    fun searchMezmurs(query: String): Flow<List<MezmurEntity>> {
        return if (query.isBlank()) {
            mezmurDao.getAllMezmurs()
        } else {
            mezmurDao.searchMezmurs(query.trim())
        }
    }

    suspend fun toggleFavorite(id: String, currentFavoriteStatus: Boolean) {
        mezmurDao.updateFavoriteStatus(id, !currentFavoriteStatus)
    }

    suspend fun addCustomMezmur(title: String, artist: String, category: String, lyrics: String) {
        val currentCount = mezmurDao.getCount() + 1
        val newEntity = MezmurEntity(
            id = "custom_${currentCount}_" + System.currentTimeMillis(),
            title = title,
            artist = if (artist.isBlank()) "የመዝሙር ደብተር" else artist,
            category = category,
            lyrics = lyrics,
            numberGeez = GeezUtil.toGeezNumeral(currentCount),
            numberInt = currentCount,
            isFavorite = false,
            lastUpdated = System.currentTimeMillis()
        )
        mezmurDao.insert(newEntity)
        updateSyncStats()

        // Optionally push to Firestore /mezmur collection if available
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docData = hashMapOf(
                "title" to newEntity.title,
                "artist" to newEntity.artist,
                "category" to newEntity.category,
                "lyrics" to newEntity.lyrics,
                "numberInt" to newEntity.numberInt,
                "numberGeez" to newEntity.numberGeez,
                "lastUpdated" to newEntity.lastUpdated
            )
            firestore.collection("mezmur").document(newEntity.id).set(docData)
        } catch (e: Exception) {
            Log.d("MezmurRepo", "Firestore sync skipped (offline mode): ${e.message}")
        }
    }

    suspend fun triggerCloudSync() {
        _isSyncing.value = true
        _syncStatus.value = "SYNC: CONNECTING TO CLOUD..."
        try {
            val existingMap = mezmurDao.getAllMezmursList().associateBy { it.id }
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("mezmur").get().await()
            val updatedList = mutableListOf<MezmurEntity>()

            for ((index, doc) in snapshot.documents.withIndex()) {
                val title = doc.getString("title") ?: continue
                val artist = doc.getString("artist") ?: "የመዝሙር ደብተር"
                val category = doc.getString("category") ?: "የምስጋና"
                val lyrics = doc.getString("lyrics") ?: ""
                val num = (doc.getLong("numberInt") ?: (index + 1).toLong()).toInt()
                val geez = doc.getString("numberGeez") ?: GeezUtil.toGeezNumeral(num)
                val existing = existingMap[doc.id]

                updatedList.add(
                    MezmurEntity(
                        id = doc.id,
                        title = title,
                        artist = artist,
                        category = category,
                        lyrics = lyrics,
                        numberGeez = geez,
                        numberInt = num,
                        isFavorite = existing?.isFavorite ?: false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            if (updatedList.isNotEmpty()) {
                mezmurDao.insertAll(updatedList)
            }
            val count = mezmurDao.getCount()
            _syncStatus.value = "SYNC: $count LYRICS CACHED • UPDATED JUST NOW"
        } catch (e: Exception) {
            Log.d("MezmurRepo", "Cloud sync fallback to local cache: ${e.message}")
            val count = mezmurDao.getCount()
            _syncStatus.value = "SYNC: $count LYRICS CACHED (100% OFFLINE)"
        } finally {
            _isSyncing.value = false
        }
    }

    private fun setupFirestoreListener() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("mezmur").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.d("MezmurRepo", "Firestore snapshot listener offline mode.")
                    return@addSnapshotListener
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val existingMap = mezmurDao.getAllMezmursList().associateBy { it.id }
                    val list = mutableListOf<MezmurEntity>()
                    for ((index, doc) in snapshot.documents.withIndex()) {
                        val title = doc.getString("title") ?: continue
                        val artist = doc.getString("artist") ?: "የመዝሙር ደብተር"
                        val category = doc.getString("category") ?: "የምስጋና"
                        val lyrics = doc.getString("lyrics") ?: ""
                        val num = (doc.getLong("numberInt") ?: (index + 1).toLong()).toInt()
                        val geez = doc.getString("numberGeez") ?: GeezUtil.toGeezNumeral(num)
                        val existing = existingMap[doc.id]

                        list.add(
                            MezmurEntity(
                                id = doc.id,
                                title = title,
                                artist = artist,
                                category = category,
                                lyrics = lyrics,
                                numberGeez = geez,
                                numberInt = num,
                                isFavorite = existing?.isFavorite ?: false,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        mezmurDao.insertAll(list)
                        updateSyncStats()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("MezmurRepo", "Firestore setup error, using Room local cache.")
        }
    }

    private suspend fun seedInitialData() {
        val defaultMezmurs = listOf(
            MezmurEntity(
                id = "m1",
                title = "ፍቅርህ ማርኮኛል",
                artist = "የዘማሪ ዲያቆን ሉልሰገድ",
                category = "የንስሐ",
                lyrics = "ፍቅርህ ማርኮኛል አምላኬ አዳኜ\nበምሕረትህ ብዛት ቆምኩኝ በደጄ\n\nከኃጢአት እሥራት ፈትተህ አዳንከኝ\nበፍቅርህ ሰንሰለት አስረህ ያዝከኝ\nአቤቱ አምላኬ እወድሃለሁ\nበቅዱስ ስምህም እመካለሁ\n\nበመከራዬ ቀን ደራሽ የሆንከው\nየሕይወቴን ድቅድቅ ብርሃን ያደረግከው\nስምህ ይባረክ ለዘለዓለሙ\nበምድር በሰማይ ይውጣ ዝናህ ፍጹሙ",
                numberGeez = "፩",
                numberInt = 1
            ),
            MezmurEntity(
                id = "m2",
                title = "አንተ ነህ መጠጊያዬ",
                artist = "የምስጋና መዝሙር",
                category = "የምስጋና",
                lyrics = "አንተ ነህ መጠጊያዬ በሰማይ በምድር\nአምላኬ ሆይ ላቅርብልህ ምስጋናና ክብር\n\nበጽኑ መከራ በብርቱ አውሎ ነፋስ\nከጐኔ አልተለየህም የጌቶች ጌታ ኢየሱስ\n\nምስጋና ይገባሃል አምላከ እሥራኤል\nከዘለዓለም እስከ ዘለዓለም ይሁን ሃሌሉያ",
                numberGeez = "፪",
                numberInt = 2
            ),
            MezmurEntity(
                id = "m3",
                title = "ቅዱስ ዑራኤል",
                artist = "የበዓላት መዝሙር",
                category = "የበዓላት",
                lyrics = "ቅዱስ ዑራኤል የብርሃን መልአክ\nበጽዋህ የተሞላውን ማይ ሕይወት አጠጣን\n\nበምልጃህ አትርፈን በጸሎትህ እርዳን\nከመከራ ሁሉ አድነን አምላክ ያክብርህ\n\nየአምላካችን መልአክ ቅዱስ ዑራኤል\nየምሕረት መጋቢ የሰላም መልአክ",
                numberGeez = "፫",
                numberInt = 3
            ),
            MezmurEntity(
                id = "m4",
                title = "ለስምህ ምስጋና ይሁን",
                artist = "ዘማሪት ፅጌማርያም",
                category = "የምስጋና",
                lyrics = "ለስምህ ምስጋና ይሁን አምላካችን\nከጥቁር አዘቅት ስላወጣኸን\n\nበኃጢአት ወድቀን በሞት ጥላ ውስጥ\nየሕይወትን መንገድ አሳየኸን ኢየሱስ\n\nምስጋና ምስጋና ይሁን ለስምህ\nለአብ ለወልድ ለመንፈስ ቅዱስም",
                numberGeez = "፬",
                numberInt = 4
            ),
            MezmurEntity(
                id = "m5",
                title = "እመቤቴ ማርያም",
                artist = "የድንግል ማርያም መዝሙር",
                category = "የድንግል ማርያም",
                lyrics = "እመቤቴ ማርያም የፍቅር እናት\nየሰማይና የምድር የብርሃን ታቦት\n\nአማልጅን ከልጅሽ ከወዳጅሽ\nከጌታችን ከኢየሱስ ክርስቶስ\n\nየአዳም ተስፋው ነሽ የሔዋን ድኅነት\nማርያም ድንግል የሰላም እናት",
                numberGeez = "፭",
                numberInt = 5
            ),
            MezmurEntity(
                id = "m6",
                title = "በስመ አብ ወወልድ",
                artist = "የኪዳን መዝሙር",
                category = "የኪዳን",
                lyrics = "በስመ አብ ወወልድ ወመንፈስ ቅዱስ\nአሐዱ አምላክ አሜን በሉ በደስታ\n\nሕይወታችንን የጠበቀ አምላካችን\nበቸርነቱ ጎበኘን አባታችን\n\nምስጋና ለሥላሴ ይሁን በሰማይ\nበምድርም ሰላም ለሰው ልጅ ሁሉ",
                numberGeez = "፮",
                numberInt = 6
            ),
            MezmurEntity(
                id = "m7",
                title = "አባታችን ሆይ",
                artist = "የንስሐ መዝሙር",
                category = "የንስሐ",
                lyrics = "በሰማያት የምትኖር አባታችን ሆይ\nስምህ ይቀደስ መንግሥትህ ትምጣ\n\nፈቃድህ በሰማይ እንደሆነች እንዲሁ በምድር ትሁን\nየዕለት እንጀራችንን ስጠን ዛሬ\nበደላችንንም ይቅር በለን",
                numberGeez = "፯",
                numberInt = 7
            ),
            MezmurEntity(
                id = "m8",
                title = "ማርያም ድንግል",
                artist = "ዘማሪ ዲያቆን ዮሴፍ",
                category = "የድንግል ማርያም",
                lyrics = "ማርያም ድንግል የሁላችን እናት\nየምሕረት መዝገብ የፍቅር ታቦት\n\nስምሽን ስጠራ ይረካል ልቤ\nምልጃሽ ያድነኛል በወጣሁ በገባሁበት\n\nቅድስት ድንግል ሆይ አትለይን\nበጸሎትሽ ጥላ ሥር ጠብቂን",
                numberGeez = "፰",
                numberInt = 8
            )
        )
        mezmurDao.insertAll(defaultMezmurs)
    }
}
