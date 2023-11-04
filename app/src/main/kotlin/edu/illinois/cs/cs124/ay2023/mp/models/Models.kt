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
open class Summary(val subject: String, val number: String, val label: String = "") : Comparable<Summary> {
    override fun compareTo(other: Summary): Int {
        // first compare by number
        // Then by subject
        // TODO("Not yet implemented")
        val numCompare = this.number.compareTo(other.number)
        return if (numCompare != 0) {
            numCompare // if the num is not same, it gonna sort now
        } else {
            this.subject.compareTo(other.subject)
            // if the num is  same, it gonna sort by the subject
        }
    }

    override fun toString() = "$subject $number: $label"
}

fun List<Summary>.filter(search: String): List<Summary> {
    return this
}
