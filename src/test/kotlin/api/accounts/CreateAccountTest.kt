package api.accounts

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import database.AccountDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
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
        val body = CreateAccountBody("id", "api key", Instant.parse("2022-12-01T15:00:00Z"))
        subject.create(body)

        verify(dao).create(body.id, body.apiKey, body.timestamp)
    }

    @Test
    fun `return status 204`() {
        val body = CreateAccountBody("id", "api key", Instant.parse("2022-12-01T15:00:00Z"))
        val res = subject.create(body)

        val expected = 204
        assertEquals(expected, res.status)
    }

}
