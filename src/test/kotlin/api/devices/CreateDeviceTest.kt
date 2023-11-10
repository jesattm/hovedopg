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
        val input = 1

        subject.create(input)

        verify(accountDao).find(1)
    }

    @Test
    fun `return status 404 if the account was not found in the database`() {
        val input = 1
        whenever(accountDao.find(any())).thenReturn(null)

        val res = subject.create(input)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `create a new device if the account was found in the database`() {
        val input = 1
        val account = Account(1)
        whenever(accountDao.find(any())).thenReturn(account)

        subject.create(input)

        verify(deviceDao).create(1)
    }

    @Test
    fun `return status 201 if the account was found in the database`() {
        val input = 1
        val account = Account(1)
        whenever(accountDao.find(any())).thenReturn(account)

        val res = subject.create(input)

        val expected = 201
        assertEquals(expected, res.status)
    }

}
