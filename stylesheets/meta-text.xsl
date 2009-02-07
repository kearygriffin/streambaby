<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">
	<xsl:apply-templates select="meta"/>
</xsl:template>

<xsl:template match="meta">
	<xsl:apply-templates select="name"/>
	<xsl:apply-templates select="tracktitle"/>
	<xsl:apply-templates select="artist"/>
	<xsl:apply-templates select="album"/>
	<xsl:apply-templates select="comment"/>
</xsl:template>

<xsl:template match="album">
	<xsl:value-of select="."/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="/meta/year"/>
	<xsl:text>&#xD;&#xA;</xsl:text>

</xsl:template>

<xsl:template match="name">
	<xsl:value-of select="."/>
	<xsl:text>&#xD;&#xA;</xsl:text>
</xsl:template>

<xsl:template match="tracktitle">
	<xsl:value-of select="."/>
	<xsl:text>&#xD;&#xA;</xsl:text>
</xsl:template>


<xsl:template match="artist">
<xsl:value-of select="."/>
<xsl:text>&#xD;&#xA;</xsl:text>
</xsl:template>

<xsl:template match="comment">
<xsl:value-of select="."/>
<xsl:text>&#xD;&#xA;</xsl:text>
</xsl:template>


</xsl:stylesheet>
