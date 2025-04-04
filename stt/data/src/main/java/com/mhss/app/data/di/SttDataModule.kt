package com.mhss.app.data.di

import com.mhss.app.domain.di.SttDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module


@Module
@ComponentScan("com.mhss.app.data")
internal class SttDataModule

val sttDataModule = module {
    includes(SttDataModule().module, SttDomainModule().module)
}
