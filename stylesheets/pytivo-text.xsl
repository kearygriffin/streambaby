<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">
<xsl:value-of select="pytivo/title"/><xsl:text>&#xD;&#xA;</xsl:text>
<xsl:value-of select="pytivo/description"/><xsl:text>&#xD;&#xA;</xsl:text>
<xsl:apply-templates select="pytivo/vActor[position() &lt; 4]"/><xsl:text>&#xD;&#xA;</xsl:text>
<xsl:apply-templates select="pytivo/vSeriesGenre"/>
</xsl:template>
<xsl:template match="pytivo/vActor">
    <xsl:variable name="thisnode" select="text()"/>
    <xsl:variable name="tokenized" select="tokenize($thisnode, '\|')"/>
    <xsl:for-each select="reverse($tokenized)">
      <xsl:value-of select="."/>
      <xsl:if test="position() != last()"><xsl:text> </xsl:text></xsl:if>
    </xsl:for-each>
      <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
</xsl:template>
<xsl:template match="pytivo/vActor/piece">
        <xsl:value-of select="."/>
        <xsl:if test="position() != last()"><xsl:text>&#x20;</xsl:text></xsl:if> 
</xsl:template>
<xsl:template match="pytivo/vSeriesGenre">
	<xsl:value-of select="."/>
	<xsl:if test="position() != last()">, </xsl:if> 
</xsl:template>
</xsl:stylesheet>
