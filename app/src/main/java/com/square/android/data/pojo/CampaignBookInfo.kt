package com.square.android.data.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class CampaignBookInfo(
               val date: String?,
               val slotId: String?)