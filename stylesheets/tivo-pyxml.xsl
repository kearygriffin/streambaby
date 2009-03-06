<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0">
<xsl:output method="xml" indent="no"/>

<xsl:template match="originalAirDate|episodeTitle|title|time|movieYear|seriesTitle|description|isEpisode|seriesId|episodeNumber|displayMajorNumber|callsign|displayMinorNumber|startTime|stopTime|partCount|partIndex">
  <xsl:variable name="name" select="name()"/>
  <xsl:element name="{$name}">
	<xsl:value-of select="."/>
  </xsl:element>
</xsl:template>

<xsl:template match="tvRating|starRating">
  <xsl:variable name="name" select="name()"/>
  <xsl:element name="{$name}">
	0<xsl:value-of select="@value"/>
  </xsl:element>
</xsl:template>

<xsl:template match="mpaaRating">
  <xsl:variable name="name" select="name()"/>
  <xsl:element name="{$name}">
    <xsl:value-of select="substring(. , 1, 1)"/>
    <xsl:value-of select="@value"/>
   </xsl:element>
</xsl:template>

<xsl:template match="vActor|vGuestStar|vDirector|vExecProducer|vProducer|vWriter|vChoreographer|vProgramGenre|vSeriesGenre">
  <xsl:for-each select="element">
    <xsl:variable name="name">
    <xsl:for-each select="parent::*">
      <xsl:if test="not(@id)">
	<xsl:value-of select="name()"/>
      </xsl:if>
      <xsl:value-of select="./@id"/>
    </xsl:for-each>
    </xsl:variable>
     <xsl:element name="{$name}">
	<xsl:value-of select="."/>
     </xsl:element>
  </xsl:for-each>
</xsl:template>

<xsl:template match="showing">
</xsl:template>

<xsl:template match="*">
<xsl:apply-templates />
</xsl:template>

<xsl:template match="text()">
</xsl:template>

<xsl:template match="/">
<pytivo>
	<xsl:apply-templates/>
</pytivo>
</xsl:template>
</xsl:stylesheet>
