package com.swift

import kotlin.jvm.Throws

class Main {

    companion object {
        //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
        // click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
        @JvmStatic
        fun main(args: Array<String>) {
            // to see how IntelliJ IDEA suggests fixing it.
            println("============================== DingTalking Action Robot ==============================\n")

            try {
                val messager = Messager(Params(if (args.isEmpty()) null else args[0]))
                println("send " + messager.send())
            } catch (e : Throwable) {
                e.printStackTrace()
            }

            println("============================== DingTalking Action Robot ==============================\n")
        }
    }
}