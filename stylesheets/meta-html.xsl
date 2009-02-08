<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" omit-xml-declaration="yes" indent="no"/>
<xsl:variable name="artwork" select="//artwork"/>
<xsl:attribute-set name="imgset">
  <xsl:attribute name="src"><xsl:value-of select="$artwork"/></xsl:attribute>
  <xsl:attribute name="align">right</xsl:attribute>
  <xsl:attribute name="width">30%</xsl:attribute>
</xsl:attribute-set>

<xsl:variable name="hasimage" select="$artwork!=''"/>

<xsl:template match="/">
	<html><body>
		<xsl:if test="$hasimage">
			<xsl:element name="img" use-attribute-sets="imgset"/>
		</xsl:if>
		<font face="arial" color="white" size="4">
			<xsl:apply-templates select="meta"/>
		</font>
	</body></html>
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
	<br/>
</xsl:template>

<xsl:template match="name">
	<xsl:value-of select="."/>
	<br/>
</xsl:template>
<xsl:template match="tracktitle">
	<xsl:value-of select="."/>
	<br/>
</xsl:template>


<xsl:template match="artist">
	<xsl:value-of select="."/>
	<br/>
</xsl:template>

<xsl:template match="comment">
	<xsl:value-of select="."/>
	<br/>
</xsl:template>


</xsl:stylesheet>
