<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0">
<xsl:output method="xml" indent="no"/>
<xsl:template match="/">
<pytivo>
	<xsl:apply-templates select="//showing/program"/>
</pytivo>
</xsl:template>

<xsl:template match="program">
	<xsl:apply-templates select="title"/>
	<xsl:apply-templates select="episodeTitle"/>
	<xsl:apply-templates select="description"/>
	<xsl:apply-templates select="vActor"/>
	<xsl:apply-templates select="vProgramGenre"/>
	<xsl:apply-templates select="vSeriesGenre"/>
</xsl:template>
<xsl:template match="title">
	<title><xsl:value-of select="."/></title>
</xsl:template>
<xsl:template match="episodeTitle">
	<episodeTitle><xsl:value-of select="."/></episodeTitle>
</xsl:template>

<xsl:template match="description">
	<description><xsl:value-of select="."/></description>
</xsl:template>
<xsl:template match="vActor">
	<xsl:apply-templates select="element"/>
</xsl:template>
<xsl:template match="vProgramGenre">
	<xsl:apply-templates select="element"/>
</xsl:template>
<xsl:template match="vSeriesGenre">
	<xsl:apply-templates select="element"/>
</xsl:template>

<xsl:template match="element">
	<xsl:variable name="parent" select="name(..)"/>
	<xsl:element name="{$parent}">
		<xsl:value-of select="."/>
	</xsl:element>
</xsl:template>
</xsl:stylesheet>
