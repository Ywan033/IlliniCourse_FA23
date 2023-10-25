@file:Suppress("MemberVisibilityCanBePrivate", "Filename", "ktlint:standard:filename")

package edu.illinois.cs.cs124.ay2023.mp.models

/**
 * Class that stores course summary information.
 *
 * Note that properties marked as private will not be handled correctly during deserialization, which is why label
 * is not marked private.
 *
 * @property subject the summary's subject
 * @property number the summary's number
 * @property label the summary's label
 */
open class Summary(val subject: String, val number: String, val label: String = "") {
    override fun toString() = "$subject $number: $label"
}
