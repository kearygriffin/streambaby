<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0">
<xsl:output method="html" indent="no"/>
<xsl:template match="/">
<html>
<head>
</head>
<body>
    <font face="arial" color="white" size="5">
    <b><xsl:value-of select="pytivo/title"/></b>
    <br/><xsl:value-of select="pytivo/description"/>
    <font size="4">
    <br/><xsl:apply-templates select="pytivo/vActor[position() &lt; 4]"/>
    <br/><i><xsl:apply-templates select="pytivo/vSeriesGenre"/></i>
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
<xsl:template match="pytivo/vSeriesGenre">
	<xsl:value-of select="."/>
	<xsl:if test="position() != last()">, </xsl:if> 
</xsl:template>
</xsl:stylesheet>
