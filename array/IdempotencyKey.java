package com.dcb;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class IdempotencyKey {

    // 1. Create a single, thread-safe random number generator.
    // This is simple, secure, and won't block Linux servers.
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // 2. Default to 24 bytes, which produces a highly secure 32-character string.
    private static final int DEFAULT_BYTES = 24;

    // Prevent anyone from accidentally creating an object of this utility class.
    private IdempotencyKey() {} 

    // --- FEATURE 1: Random URL-Safe Key ---
    // Generates a random text string that is safe to use in web URLs or HTTP Headers.
    public static String randomKey() {
        return randomKey(DEFAULT_BYTES);
    }

    public static String randomKey(int numBytes) {
        if (numBytes < 16) {
            throw new IllegalArgumentException("Need at least 16 bytes for security.");
        }
        
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes); // Fill the array with secure random gibberish
        
        // Convert the random bytes into a URL-safe string, removing the '=' padding at the end.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // --- FEATURE 2: Timestamped Key ---
    // Adds the current system time to the front of a random key. 
    // This makes it incredibly easy to sort or search for keys in your system logs.
    public static String timestampedKey() {
        return System.currentTimeMillis() + "-" + randomKey();
    }

    // --- FEATURE 3: HMAC-Bound Key ---
    // Creates a key mathematically tied to the specific request data (like payment amount).
    // If a hacker intercepts the request and tries to change the amount using the same key, it will fail.
    public static String hmacBoundKey(byte[] secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HMAC key", e);
        }
    }

    // --- FEATURE 4: Combined Key ---
    // The ultimate "belt-and-suspenders" key: Time + Randomness + Tied to the request data.
    public static String combinedKey(byte[] secret, String requestData, int numBytes) {
        String time = String.valueOf(System.currentTimeMillis());
        String random = randomKey(numBytes);
        
        // Hash all these pieces together so they cannot be tampered with.
        String hash = hmacBoundKey(secret, time + "|" + requestData + "|" + random);
        
        return time + "-" + random + "-" + hash;
    }

    // --- FEATURE 5: Simple SHA-256 Hashing ---
    // A helper function to turn any text string into a secure, URL-safe hash.
    public static String sha256Base64Url(String input) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(input.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hash failed", e);
        }
    }

    // --- DEMO / TESTING ---
    public static void main(String[] args) {
        System.out.println("1. Random:      " + randomKey());
        System.out.println("2. Timestamped: " + timestampedKey());

        // Simulated payment data
        String data = "merchant=Flipkart|amount=123456|currency=INR";
        byte[] secret = "super-secret-key".getBytes(StandardCharsets.UTF_8);

        System.out.println("3. Bound:       " + hmacBoundKey(secret, data));
        System.out.println("4. Combined:    " + combinedKey(secret, data, 24));
    }
}
