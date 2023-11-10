package api.accounts

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Account
import database.AccountDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CreateAccountTest {

    private lateinit var dao: AccountDao
    private lateinit var subject: CreateAccount

    @BeforeEach
    fun setup() {
        dao = mock()

        subject = CreateAccount(dao)
    }

    @Test
    fun `create a new account in the database`() {
        subject.create()

        verify(dao).create()
    }

    @Test
    fun `return status 201`() {
        val res = subject.create()

        val expected = 201
        assertEquals(expected, res.status)
    }

    @Test
    fun `return created account`() {
        val id = 1
        whenever(dao.create()).thenReturn(id)

        val res = subject.create()

        val expected = Account(id)
        assertEquals(expected, res.entity)
    }

}
