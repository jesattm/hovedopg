package api.devices

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Account
import database.AccountDao
import database.DeviceDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CreateDeviceTest {

    private lateinit var deviceDao: DeviceDao
    private lateinit var accountDao: AccountDao
    private lateinit var subject: CreateDevice

    @BeforeEach
    fun setup() {
        deviceDao = mock()
        accountDao = mock()

        subject = CreateDevice(accountDao, deviceDao)
    }

    @Test
    fun `look up the account in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id")

        subject.create(accountIdParam, body)

        verify(accountDao).find(accountIdParam)
    }

    @Test
    fun `return status 404 if the account was not found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id")
        whenever(accountDao.find(any())).thenReturn(null)

        val res = subject.create(accountIdParam, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `create a new device if the account was found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id")
        val account = Account("id", "api key")
        whenever(accountDao.find(any())).thenReturn(account)

        subject.create(accountIdParam, body)

        verify(deviceDao).create(body.id, accountIdParam)
    }

    @Test
    fun `return status 204 if the account was found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id")
        val account = Account("id", "api key")
        whenever(accountDao.find(any())).thenReturn(account)

        val res = subject.create(accountIdParam, body)

        val expected = 204
        assertEquals(expected, res.status)
    }

}
