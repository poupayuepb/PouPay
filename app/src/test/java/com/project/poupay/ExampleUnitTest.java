package com.project.poupay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.project.poupay.tools.FieldValidator;

import org.junit.Test;

public class ExampleUnitTest {
    @Test
    public void field_validation() {
        // Valid usernames
        assertTrue(FieldValidator.validate("nome.usuario", FieldValidator.TYPE_USERNAME));
        assertTrue(FieldValidator.validate("nome_usuario", FieldValidator.TYPE_USERNAME));
        assertTrue(FieldValidator.validate("abcdeabcde", FieldValidator.TYPE_USERNAME));
        assertTrue(FieldValidator.validate("aaaaaa_bbbbbb", FieldValidator.TYPE_USERNAME));
        assertTrue(FieldValidator.validate("abc123.abc", FieldValidator.TYPE_USERNAME));

        // Valid passwords
        assertTrue(FieldValidator.validate("Password123@", FieldValidator.TYPE_PASSWORD));
        assertTrue(FieldValidator.validate("pass@123Aaa", FieldValidator.TYPE_PASSWORD));
        assertTrue(FieldValidator.validate("0Aa#1234", FieldValidator.TYPE_PASSWORD));
        assertTrue(FieldValidator.validate("Senha123*", FieldValidator.TYPE_PASSWORD));
        assertTrue(FieldValidator.validate("##$Enha0", FieldValidator.TYPE_PASSWORD));

        // Invalid usernames
        assertFalse(FieldValidator.validate("nome usuario", FieldValidator.TYPE_USERNAME));
        assertFalse(FieldValidator.validate(" nome usuario ", FieldValidator.TYPE_USERNAME));
        assertFalse(FieldValidator.validate("nome", FieldValidator.TYPE_USERNAME));
        assertFalse(FieldValidator.validate("usuario_.nome", FieldValidator.TYPE_USERNAME));
        assertFalse(FieldValidator.validate("0000", FieldValidator.TYPE_USERNAME));

        // Invalid passwords
        assertFalse(FieldValidator.validate("password123@", FieldValidator.TYPE_PASSWORD));
        assertFalse(FieldValidator.validate("pass", FieldValidator.TYPE_PASSWORD));
        assertFalse(FieldValidator.validate("senha123", FieldValidator.TYPE_PASSWORD));
        assertFalse(FieldValidator.validate("Senha123", FieldValidator.TYPE_PASSWORD));
        assertFalse(FieldValidator.validate("Senha@@@", FieldValidator.TYPE_PASSWORD));
        assertFalse(FieldValidator.validate("senha@@@", FieldValidator.TYPE_PASSWORD));


    }

    @Test
    public void add_validation(){

    }
}