package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_NoteDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.AddNoteFolderUseCase(noteRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.AddNoteUseCase(notesRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteNoteFolderUseCase(noteRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteNoteUseCase(repository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllFolderlessNotesUseCase(notesRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllNoteFoldersUseCase(repository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetNoteFolderUseCase(repository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetNoteUseCase(notesRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetNotesByFolderUseCase(notesRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.SearchNotesUseCase(notesRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.UpdateNoteFolderUseCase(noteRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.UpdateNoteUseCase(notesRepository=get()) } 
}
public val com.mhss.app.domain.di.NoteDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_NoteDomainModule
