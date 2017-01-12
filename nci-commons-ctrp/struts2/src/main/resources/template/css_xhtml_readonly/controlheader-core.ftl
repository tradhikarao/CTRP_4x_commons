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
<#--
	Only show message if errors are available.
	This will be done if ActionSupport is used.
-->
<div <#rt/><#if parameters.id?exists>id="wwgrp_${parameters.id}"<#rt/></#if> class="wwgrp">
	
<#if parameters.label?exists>
<#if parameters.labelposition?default("top") == 'top'>
<div <#rt/>
<#else>
<span <#rt/>
</#if>
<#if parameters.id?exists>id="wwlbl_${parameters.id}"<#rt/></#if> class="wwlbl">
    <label <#t/>
<#if parameters.id?exists>
        for="${parameters.id?html}" <#t/>
</#if>
        class="label"<#t/>
    ><#t/>
<#if parameters.required?default(false)>
        <span class="required">*</span><#t/>
</#if>
        ${parameters.label?html}:
<#include "/${parameters.templateDir}/xhtml/tooltip.ftl" />
	</label><#t/>
<#if parameters.labelposition?default("top") == 'top'>
</div> <br /><#rt/>
<#else>
</span> <#rt/>
</#if>
</#if>
