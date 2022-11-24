package dev.schlaubi.icetracker

import io.jenetics.jpx.XMLProvider
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory

class XMLDocumentBuilder : XMLProvider() {
    override fun documentBuilderFactory(): DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    override fun xmlOutputFactory(): XMLOutputFactory = XMLOutputFactory.newInstance()
    override fun xmlInputFactory(): XMLInputFactory = XMLInputFactory.newInstance()
}
