package com.example.echo_server

import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

object Retranslator {

    private val publisher: ReplaySubject<String> = ReplaySubject.create(50)
    val logs: Observable<String> = publisher

    fun send(msg: String) {
        publisher.onNext(msg)
    }
}