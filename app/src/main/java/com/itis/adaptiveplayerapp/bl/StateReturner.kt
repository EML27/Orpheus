package com.itis.adaptiveplayerapp.bl

import com.itis.adaptiveplayerapp.bl.dto.StateDto
import com.itis.adaptiveplayerapp.bl.gps.UserOccupation
import com.itis.adaptiveplayerapp.di.component.DaggerStateComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateReturner @Inject constructor(){

    init {
        DaggerStateComponent.create().inject(this)
    }

    @Inject
    lateinit var userOccupation: UserOccupation

    fun getOccupation() =
        userOccupation.getCurrentOccupation()


    fun getState(): StateDto {
        return StateDto(occupation = getOccupation())
    }
}