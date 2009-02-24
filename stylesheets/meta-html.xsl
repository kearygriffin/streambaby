<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" omit-xml-declaration="yes" indent="no"/>
<!-- xsl:param name="stylesheet">default.css</xsl:param -->
<xsl:variable name="artwork" select="//artwork"/>
<xsl:attribute-set name="imgset">
  <xsl:attribute name="src"><xsl:value-of select="$artwork"/></xsl:attribute>
</xsl:attribute-set>

<xsl:variable name="hasimage" select="$artwork!=''"/>

<xsl:template match="/">
	<html><head></head><body>
		<xsl:if test="$hasimage">
		       <xsl:element name="img" use-attribute-sets="imgset"/>
		</xsl:if>
		<div id="global">
			<xsl:apply-templates select="meta"/>
		</div>
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
  <p>
    <xsl:value-of select="."/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="/meta/year"/>
  </p>
</xsl:template>

<xsl:template match="name">
  <h1><xsl:value-of select="."/></h1>
</xsl:template>
<xsl:template match="tracktitle">
  <h2><xsl:value-of select="."/></h2>
</xsl:template>


<xsl:template match="artist">
  <p><xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="comment">
  <p><xsl:value-of select="."/></p>
</xsl:template>


</xsl:stylesheet>
