<#--
/*
 * $Id: Action.java 502296 2007-02-01 17:33:39Z niallp $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
-->
<#--include "/${parameters.templateDir}/css_xhtml/controlheader.ftl" /-->
<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />

<#if parameters.multiple?default(false) && parameters.nameValue?exists>
    <#assign selectedSize = parameters.nameValue?size>
<#else>
    <#assign selectedSize = 0>
</#if>
<#assign selectedCount = 0/>

<@s.iterator value="parameters.list">
        <#if parameters.listKey?exists>
            <#if stack.findValue(parameters.listKey)?exists>
              <#assign itemKey = stack.findValue(parameters.listKey)/>
              <#assign itemKeyStr = itemKey.toString()/>
            <#else>
              <#assign itemKey = ''/>
              <#assign itemKeyStr = ''/>
            </#if>
        <#else>
            <#assign itemKey = stack.findValue('top')/>
            <#assign itemKeyStr = itemKey.toString()/>
        </#if>
        <#if parameters.listValue?exists>
            <#if stack.findString(parameters.listValue)?exists>
              <#assign itemValue = stack.findString(parameters.listValue)/>
            <#else>
              <#assign itemValue = ''/>
            </#if>
        <#else>
            <#assign itemValue = stack.findString('top')/>
        </#if>
        <#if tag.contains(parameters.nameValue, itemKey) == true>
            <#assign selectedCount = selectedCount + 1/>
            ${itemValue?html}<#if selectedCount < selectedSize>, </#if>
        </#if>
</@s.iterator>

<#include "/${parameters.templateDir}/css_xhtml/controlfooter.ftl" />
