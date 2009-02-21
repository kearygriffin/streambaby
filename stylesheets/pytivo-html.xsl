<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0">
<xsl:output method="html" indent="no"/>

<xsl:variable name="artwork" select="//image"/>
<xsl:attribute-set name="imgset">
  <xsl:attribute name="src"><xsl:value-of select="$artwork"/></xsl:attribute>
  <xsl:attribute name="align">right</xsl:attribute>
  <xsl:attribute name="width">30%</xsl:attribute>
</xsl:attribute-set>

<xsl:variable name="hasimage" select="$artwork!=''"/>



<xsl:template match="/">
<html>
<head>
</head>
<body>
   <xsl:if test="$hasimage">
       <xsl:element name="img" use-attribute-sets="imgset"/>
   </xsl:if>

    <font face="arial" color="white" size="3">
    <b>
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

    </b>
    <br/><xsl:value-of select="pytivo/description"/>
    <font size="3">
    <br/><xsl:apply-templates select="pytivo/vActor[position() &lt; 4]"/>
    <br/><i><xsl:apply-templates select="pytivo/vProgramGenre"/></i>
    </font></font>
</body>
</html>
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
