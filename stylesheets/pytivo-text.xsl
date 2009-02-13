<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">
<xsl:choose>
      <xsl:when test="pytivo/isEpisode">
        <xsl:choose>
          <xsl:when test="pytivo/seriesTitle and pytivo/episodeTitle">
            <xsl:value-of select="pytivo/seriesTitle"/> - <xsl:value-of select="pytivo/episodeTitle"/>
          </xsl:when>
          <xsl:when test="pytivo/episodeTitle">
            <xsl:value-of select="pytivo/episodeTitle"/>
          </xsl:when>
          <xsl:when test="pytivo/seriesTitle">
            <xsl:value-of select="pytivo/seriesTitle"/>
          </xsl:when>
          <xsl:when test="pytivo/title">
            <xsl:value-of select="pytivo/title"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Default Title Here?? -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="pytivo/title">
        <xsl:value-of select="pytivo/title"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Default Title Here?? -->
      </xsl:otherwise>
  </xsl:choose>
      <xsl:text>&#xD;&#xA;</xsl:text>

	<!-- xsl:value-of select="pytivo/title"/><xsl:text>&#xD;&#xA;</xsl:text -->
	<xsl:value-of select="pytivo/description"/><xsl:text>&#xD;&#xA;</xsl:text>
	<xsl:apply-templates select="pytivo/vActor[position() &lt; 4]"/><xsl:text>&#xD;&#xA;</xsl:text>
	<xsl:apply-templates select="pytivo/vProgramGenre"/>
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
<xsl:template match="pytivo/vProgramGenre">
	<xsl:value-of select="."/>
	<xsl:if test="position() != last()">, </xsl:if> 
</xsl:template>
</xsl:stylesheet>
