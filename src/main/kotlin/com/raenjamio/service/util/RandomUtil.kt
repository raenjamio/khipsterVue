@file:JvmName("RandomUtil")

package com.raenjamio.service.util

import java.security.SecureRandom
import org.apache.commons.lang3.RandomStringUtils

private const val DEF_COUNT = 20

private val secureRandom: SecureRandom = SecureRandom().apply { nextBytes(ByteArray(64)) }

private fun generateRandomAlphanumericString() = RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, null, secureRandom)

/**
* Generate a password.
*
* @return the generated password.
*/
fun generatePassword() = generateRandomAlphanumericString()

/**
* Generate an activation key.
*
* @return the generated activation key.
*/
fun generateActivationKey() = generateRandomAlphanumericString()

/**
* Generate a reset key.
*
* @return the generated reset key.
*/
fun generateResetKey(): String = generateRandomAlphanumericString()

/**
* Generate a unique series to validate a persistent token, used in the
* authentication remember-me mechanism.
*
* @return the generated series data.
*/
fun generateSeriesData(): String = generateRandomAlphanumericString()

/**
* Generate a persistent token, used in the authentication remember-me mechanism.
*
* @return the generated token data.
*/
fun generateTokenData(): String = generateRandomAlphanumericString()
