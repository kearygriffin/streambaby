<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0">
<xsl:output method="html" indent="no"/>

<!-- xsl:param name="stylesheet">default.css</xsl:param -->
<xsl:variable name="artwork" select="//image"/>
<xsl:attribute-set name="imgset">
  <xsl:attribute name="src"><xsl:value-of select="$artwork"/></xsl:attribute>
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

   <div id="global">

<h1>
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
</h1>
<p><xsl:value-of select="pytivo/description"/></p>

   <p>
   <xsl:if test="count(pytivo/time)"> 
    <xsl:apply-templates select="pytivo/time"/><xsl:text> </xsl:text>
   </xsl:if>
   <xsl:if test="count(pytivo/displayMajorNumber)"> 
    <xsl:apply-templates select="pytivo/displayMajorNumber"/><xsl:text> </xsl:text>
   </xsl:if>
   <xsl:if test="count(pytivo/callsign)"> 
    <xsl:apply-templates select="pytivo/callsign"/>
   </xsl:if>
   
   <xsl:if test="count(pytivo/originalAirDate)">
    <br/>Original Air Date: <xsl:apply-templates select="pytivo/originalAirDate"/>
   </xsl:if>
   
   <xsl:if test="count(pytivo/duration)">
    <br/>Duration: <xsl:apply-templates select="pytivo/duration"/>
   </xsl:if>
   
   <xsl:if test="count(pytivo/episodeNumber)">
    <br/>Episode #: <xsl:apply-templates select="pytivo/episodeNumber"/>
   </xsl:if>
   </p>
   
   <xsl:if test="count(pytivo/vActor)"> 
    <p><xsl:apply-templates select="pytivo/vActor[position() &lt; 4]"/></p>
   </xsl:if>
   <xsl:if test="count(pytivo/vProgramGenre)"> 
    <p class="genre"><xsl:apply-templates select="pytivo/vProgramGenre"/></p>
   </xsl:if>
</div>
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
      <xsl:if test="position() != last() and position() &lt; 3"><xsl:text>, </xsl:text></xsl:if>
</xsl:template>
<xsl:template match="pytivo/vProgramGenre">
	<xsl:value-of select="."/>
	<xsl:if test="position() != last()">, </xsl:if> 
</xsl:template>
<xsl:template match="pytivo/time">
    <xsl:variable name="time" select="format-dateTime(adjust-dateTime-to-timezone(text()), '[F,*-3] [M]/[D] [h,2]:[m,2] [Pn,*-2]')"/>
    <xsl:value-of select="$time"/>
</xsl:template>
<xsl:template match="pytivo/originalAirDate">
    <xsl:variable name="time" select="format-dateTime(text(), '[M,2]/[D,2]/[Y,4]')"/>
    <xsl:value-of select="$time"/>
</xsl:template>
<xsl:template match="pytivo/duration">
    <xsl:variable name="hours" select="hours-from-duration(text())"/>
    <xsl:variable name="mins" select="minutes-from-duration(text())"/>
    <xsl:value-of select="$hours"/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select="format-number($mins, '00')"/>
</xsl:template>
</xsl:stylesheet>
