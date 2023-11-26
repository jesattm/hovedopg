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
import java.time.Instant
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
        val body = CreateDeviceBody("id", Instant.parse("2022-12-01T15:00:00Z"))

        subject.create(accountIdParam, body)

        verify(accountDao).find(accountIdParam)
    }

    @Test
    fun `return status 404 if the account was not found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id", Instant.parse("2022-12-01T15:00:00Z"))
        whenever(accountDao.find(any())).thenReturn(null)

        val res = subject.create(accountIdParam, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `create a new device if the account was found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id", Instant.parse("2022-12-01T15:00:00Z"))
        val account = Account("id", "api key", null)
        whenever(accountDao.find(any())).thenReturn(account)

        subject.create(accountIdParam, body)

        verify(deviceDao).create(body.id, accountIdParam, body.timestamp)
    }

    @Test
    fun `return status 204 if the account was found in the database`() {
        val accountIdParam = "account id"
        val body = CreateDeviceBody("id", Instant.parse("2022-12-01T15:00:00Z"))
        val account = Account("id", "api key", null)
        whenever(accountDao.find(any())).thenReturn(account)

        val res = subject.create(accountIdParam, body)

        val expected = 204
        assertEquals(expected, res.status)
    }

}
