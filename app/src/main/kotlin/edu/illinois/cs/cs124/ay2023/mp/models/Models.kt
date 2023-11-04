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
        if (numCompare == 0) {
            // if the num is not same, it gonna sort now
            return this.subject.compareTo(other.subject)
        } else {
            return numCompare
        }
    }

    override fun toString() = "$subject $number: $label"
}

fun List<Summary>.filter(search: String): List<Summary> {
    // trim the search string
    val trimedsearchName = search.lowercase().trim()

    // filter the list and check if it contains the "trimedsearchName"
    val filteredList = this.filter { it.toString().lowercase().contains(trimedsearchName) }

    // the first default sort
    val firstSort = filteredList.sorted()

    val finalSort = firstSort.sortedBy { summary ->
        summary.toString().lowercase().indexOf(trimedsearchName)
    } // sort the summaries by the position of the search term, with earlier matches appearing first.
    return finalSort
}

