package org.gameswap.security;

import org.junit.Test;

import static org.gameswap.security.PasswordService.checkPassword;
import static org.gameswap.security.PasswordService.hashPassword;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordServiceTest {

    @Test
    public void testNewPasswordCanBeCheckedAgainstHash() {
        String hash = hashPassword("testpassword");
        assertTrue("password check against hash failed", checkPassword("testpassword", hash));
    }

    @Test
    public void testExistingHashCanBeChecked() {
        String hash = "$2a$10$DadEkVkACr/knT3sa16./e7rAp53tdkvX58.v2C/OmgAG8y/dnQCa"; //hash for password 'testpassword'
        assertTrue("password check against existing hash failed", checkPassword("testpassword", hash));
        assertFalse("checking incorrect password against hash succeeded", checkPassword("wonkypass", hash));
    }
}