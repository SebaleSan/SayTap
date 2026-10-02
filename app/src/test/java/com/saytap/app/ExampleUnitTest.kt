package com.saytap.app

import org.junit.Test

import org.junit.Assert.*
import com.saytap.app.util.esPasswordValida
import com.saytap.app.util.requisitosFaltantesPassword

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun passwordValida_cumpleTodosLosRequisitos() {
        assertTrue("Abcdef12".esPasswordValida())
    }

    @Test
    fun passwordInvalida_devuelveLosTresRequisitosFaltantes() {
        assertEquals(
            listOf("mínimo 8 caracteres", "al menos una mayúscula", "al menos un número"),
            "abc".requisitosFaltantesPassword()
        )
    }

    @Test
    fun passwordInvalida_noEsValida() {
        assertFalse("abc".esPasswordValida())
    }

    @Test
    fun passwordSinMayuscula_noEsValida() {
        assertEquals(listOf("al menos una mayúscula"), "abcdef12".requisitosFaltantesPassword())
    }

    @Test
    fun passwordSinNumero_noEsValida() {
        assertEquals(listOf("al menos un número"), "Abcdefgh".requisitosFaltantesPassword())
    }

    @Test
    fun passwordMuyCorta_noEsValida() {
        assertEquals(listOf("mínimo 8 caracteres"), "Abc1".requisitosFaltantesPassword())
    }
}