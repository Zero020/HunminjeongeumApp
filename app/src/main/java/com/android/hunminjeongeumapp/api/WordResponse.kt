package com.android.hunminjeongeumapp.api

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "channel", strict = false)
data class WordResponse(
    @field:ElementList(entry = "item", inline = true, required = false)
    var items: List<WordItem>? = null
)
@Root(name = "item", strict = false)
data class WordItem(
    @field:Element(name = "word", required = false)
    var word: String? = null,

    @field:ElementList(entry = "sense", inline = true, required = false)
    var senses: List<SenseItem>? = null,

    @field:Element(name = "example", required = false)
    var example: String? = null
)

@Root(name = "sense", strict = false)
data class SenseItem(
    @field:Element(name = "definition", required = false)
    var definition: String? = null
)




