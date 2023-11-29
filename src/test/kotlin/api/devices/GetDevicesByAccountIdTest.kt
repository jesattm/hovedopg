package api.devices

import database.Account
import database.AccountDao
import database.Device
import database.DeviceDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class GetDevicesByAccountIdTest {

    private lateinit var deviceDao: DeviceDao
    private lateinit var accountDao: AccountDao
    private lateinit var subject: GetDevicesByAccountId

    @BeforeEach
    fun setup() {
        deviceDao = mock()
        accountDao = mock()

        subject = GetDevicesByAccountId(accountDao, deviceDao)
    }

    @Test
    fun `return status 404 if the account was not found in the database`() {
        val input = "account id"
        whenever(accountDao.find(any())).thenReturn(null)

        val res = subject.get(input)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `find all devices in the database with the input account id`() {
        val input = "account id"
        val account = Account("account id", "api key")
        whenever(accountDao.find(any())).thenReturn(account)

        subject.get(input)

        verify(deviceDao).findByAccountId(input)
    }

    @Test
    fun `return status 200 if the request is successful`() {
        val input = "1"
        val account = Account("1", "api key")
        val device1 = Device("1", "1")
        val device2 = Device("2", "1")
        val devices = listOf(device1, device2)
        whenever(accountDao.find(any())).thenReturn(account)
        whenever(deviceDao.findByAccountId(any())).thenReturn(devices)

        val res = subject.get(input)

        val expected = 200
        assertEquals(expected, res.status)
    }

}
