package com.example.smartzone.helpers

import com.example.smartzone.entities.Zone
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class ZonesHelperTests {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var zonesHelper: ZonesHelper
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var task: Task<Void>
    private val userId = "user1"


    @Before
    fun setUp() {
        auth = mock()
        firestore = mock()
        zonesHelper = ZonesHelper(auth = auth, db = firestore)

        collectionReference = mock()
        documentReference = mock()
        task = mock()

        currentUser = mock()
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn(userId)

        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document(userId)).thenReturn(documentReference)
        whenever(documentReference.collection("zones")).thenReturn(collectionReference)
        whenever(collectionReference.document()).thenReturn(documentReference)
    }

    @Test
    fun sortZones_listOfZones_newestFirst() {
        //Arrange
        val now = Timestamp.now()
        val zone1 = Zone("zone1", "Test zone 1", "Testing", now)

        val zone2 =
            Zone("zone2", "Test zone 2", "Testing", Timestamp(now.seconds + 10, now.nanoseconds))
        val zones = listOf(zone1, zone2)

        //Act
        val sortedZones = zonesHelper.sortZones(zones, newestFirst = true)

        //Assert
        assertEquals(zone2.id, sortedZones[0].id)
        assertEquals(zone1.id, sortedZones[1].id)
    }

    @Test
    fun sortZones_listOfZones_oldestFirst() {
        //Arrange
        val now = Timestamp.now()
        val zone1 = Zone("zone1", "Test zone 1", "Testing", now)

        val zone2 =
            Zone("zone2", "Test zone 2", "Testing", Timestamp(now.seconds + 10, now.nanoseconds))
        val zones = listOf(zone1, zone2)

        //Act
        val sortedZones = zonesHelper.sortZones(zones, newestFirst = false)

        //Assert
        assertEquals(zone2.id, sortedZones[1].id)
        assertEquals(zone1.id, sortedZones[0].id)
    }

    @Test
    fun sortZones_emptyList_returnsEmptyList() {
        // Arrange
        val zones: List<Zone> = emptyList()

        // Act
        val sortedZones = zonesHelper.sortZones(zones, newestFirst = true)

        // Assert
        assert(sortedZones.isEmpty())
    }

    @Test
    fun filterZones_listOfZones_filterByName() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zones = listOf(zone1, zone2)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "Testing Zone")

        // Assert
        assertEquals(1, filteredZones.size)
        assertEquals(zone1.id, filteredZones[0].id)
    }

    @Test
    fun filterZones_listOfZones_filteredbyFocus() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zones = listOf(zone1, zone2)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "Mobile app")

        // Assert
        assertEquals(1, filteredZones.size)
        assertEquals(zone2.id, filteredZones[0].id)
    }

    @Test
    fun filterZones_noMatchingZones_returnsEmptyList() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zones = listOf(zone1, zone2)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "Desktop")

        // Assert
        assert(filteredZones.isEmpty())
    }

    @Test
    fun filterZones_multipleMatches_returnsMatchingZones() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zone3 = Zone("zone3", "Software Zone", "Desktop app", Timestamp.now())
        val zones = listOf(zone1, zone2, zone3)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "Software Zone")

        // Assert
        assertEquals(2, filteredZones.size)
        assert(filteredZones.any { it.id == zone2.id })
        assert(filteredZones.any { it.id == zone3.id })
    }

    @Test
    fun filterZones_caseInsensitiveMatch_returnsMatchingZone() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zones = listOf(zone1, zone2)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "testing")

        // Assert
        assertEquals(1, filteredZones.size)
        assertEquals(zone1.id, filteredZones[0].id)
    }

    @Test
    fun filterZones_emptyQuery_returnsAllZones() {
        // Arrange
        val zone1 = Zone("zone1", "Testing Zone", "Testing", Timestamp.now())
        val zone2 = Zone("zone2", "Software Zone", "Mobile app", Timestamp.now())
        val zones = listOf(zone1, zone2)

        // Act
        val filteredZones = zonesHelper.filterZones(zones, "")

        // Assert
        assertEquals(2, filteredZones.size)
        assert(filteredZones.any { it.id == zone1.id })
        assert(filteredZones.any { it.id == zone2.id })
    }

    @Test
    fun createZone_firestoreSuccessAndValidData_callsOnSuccess() {
        //Arrange
        val name = "Testing Zone"
        val focus = "Testing"

        val generatedId = "generatedZoneId"
        whenever(documentReference.id).thenReturn(generatedId)
        whenever(documentReference.set(any())).thenReturn(task)

        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            task
        }

        var successCalled = false
        var failureMessage: String? = null

        //Act
        zonesHelper.createZone(userId, name, focus,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it.message })

        //Assert
        assert(successCalled)
        assertNull(failureMessage)
    }

    @Test
    fun createZone_missingName_callsOnFailure() {
        // Arrange
        val focus = "Testing"
        val name: String? = null

        var successCalled = false
        var failureMessage: String? = null

        // Act
        zonesHelper.createZone(userId, name ?: "", focus,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it.message })

        // Assert
        assertFalse(successCalled)
        assertNotNull(failureMessage)
        assertEquals("Name cannot be empty", failureMessage)
    }

    @Test
    fun createZone_missingFocus_callsOnSuccess() {
        // Arrange
        val name = "Testing Zone"
        val focus: String? = null

        var successCalled = false
        var failureMessage: String? = null

        val generatedId = "generatedZoneId"
        whenever(documentReference.id).thenReturn(generatedId)
        whenever(documentReference.set(any())).thenReturn(task)

        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            task
        }

        // Act
        zonesHelper.createZone(userId, name, focus ?: "",
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it.message })

        // Assert
        assert(successCalled)
        assertNull(failureMessage)
    }


    @Test
    fun createZones_firestoreFailure_callsOnFailure() {
        //Arrange
        val name = "Testing Zone"
        val focus = "Testing"


        val generatedId = "generatedZoneId"
        whenever(documentReference.id).thenReturn(generatedId)
        whenever(documentReference.set(any())).thenReturn(task)

        val exception = Exception("Firestore error")
        whenever(task.addOnSuccessListener(any())).thenReturn(task)
        whenever(task.addOnFailureListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnFailureListener
            listener.onFailure(exception)
            task
        }

        var successCalled = false
        var failureMessage: String? = null

        //Act
        zonesHelper.createZone(userId, name, focus,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it.message })

        //Assert
        assertFalse(successCalled)
        assertEquals("Firestore error", failureMessage)
    }


}