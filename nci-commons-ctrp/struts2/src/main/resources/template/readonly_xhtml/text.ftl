<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />

<#if parameters.nameValue?exists>
  <@s.property value="parameters.nameValue"/>
</#if>

<#include "/${parameters.templateDir}/readonly_xhtml/controlfooter.ftl" />

    