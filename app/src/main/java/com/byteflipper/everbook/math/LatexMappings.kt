/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

internal object LatexMappings {
    private val map = mapOf(
        // Greek lowercase
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "epsilon" to "ε",
        "zeta" to "ζ", "eta" to "η", "theta" to "θ", "iota" to "ι", "kappa" to "κ",
        "lambda" to "λ", "mu" to "μ", "nu" to "ν", "xi" to "ξ", "omicron" to "ο",
        "pi" to "π", "rho" to "ρ", "sigma" to "σ", "tau" to "τ", "upsilon" to "υ",
        "phi" to "φ", "chi" to "χ", "psi" to "ψ", "omega" to "ω",
        // Greek uppercase (few common)
        "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ", "Xi" to "Ξ", "Pi" to "Π", "Sigma" to "Σ", "Phi" to "Φ", "Psi" to "Ψ", "Omega" to "Ω",
        // operations / relations
        "pm" to "±", "times" to "×", "div" to "÷", "le" to "≤", "ge" to "≥", "neq" to "≠",
        "approx" to "≈", "infty" to "∞", "rightarrow" to "→", "leftarrow" to "←", "cdot" to "·",
        "leq" to "≤", "geq" to "≥", "equiv" to "≡", "propto" to "∝", "sim" to "∼", "simeq" to "≃",
        "subset" to "⊂", "supset" to "⊃", "subseteq" to "⊆", "supseteq" to "⊇", "in" to "∈", "notin" to "∉",
        "cup" to "∪", "cap" to "∩", "emptyset" to "∅", "forall" to "∀", "exists" to "∃", "nexists" to "∄",
        // calculus symbols
        "sum" to "∑", "int" to "∫", "lim" to "lim", "prod" to "∏", "partial" to "∂", "nabla" to "∇",
        "inf" to "∞", "min" to "min", "max" to "max", "sup" to "sup", "inf" to "inf",
        // functions
        "sin" to "sin", "cos" to "cos", "tan" to "tan", "cot" to "cot", "sec" to "sec", "csc" to "csc",
        "arcsin" to "arcsin", "arccos" to "arccos", "arctan" to "arctan", "sinh" to "sinh", "cosh" to "cosh", "tanh" to "tanh",
        "log" to "log", "ln" to "ln", "exp" to "exp", "sqrt" to "√",
        // arrows
        "Rightarrow" to "⇒", "Leftarrow" to "⇐", "Leftrightarrow" to "⇔", "iff" to "⇔",
        "mapsto" to "↦", "to" to "→", "gets" to "←",
        // brackets
        "langle" to "⟨", "rangle" to "⟩", "lceil" to "⌈", "rceil" to "⌉", "lfloor" to "⌊", "rfloor" to "⌋",
        // misc
        "ldots" to "…", "cdots" to "⋯", "vdots" to "⋮", "ddots" to "⋱", "prime" to "′", "doubleprime" to "″",
        "hbar" to "ℏ", "ell" to "ℓ", "Re" to "ℜ", "Im" to "ℑ", "wp" to "℘", "aleph" to "ℵ"
    )

    // Дополнительные математические символы
    private val additionalMap = mapOf(
        // Операторы
        "oplus" to "⊕", "ominus" to "⊖", "otimes" to "⊗", "oslash" to "⊘", "odot" to "⊙",
        "bigoplus" to "⨁", "bigotimes" to "⨂", "bigodot" to "⨀",
        // Стрелки
        "uparrow" to "↑", "downarrow" to "↓", "Uparrow" to "⇑", "Downarrow" to "⇓",
        "leftrightarrow" to "↔", "Leftrightarrow" to "⇔", "longleftarrow" to "⟵", "longrightarrow" to "⟶",
        "Longleftarrow" to "⟸", "Longrightarrow" to "⟹", "longleftrightarrow" to "⟷", "Longleftrightarrow" to "⟺",
        // Логические операторы
        "land" to "∧", "lor" to "∨", "lnot" to "¬", "implies" to "⟹", "iff" to "⟺",
        // Множества
        "emptyset" to "∅", "varnothing" to "∅", "mathbbm{1}" to "𝟙",
        // Геометрия
        "angle" to "∠", "measuredangle" to "∡", "sphericalangle" to "∢",
        "triangle" to "△", "square" to "□", "diamond" to "◇", "circle" to "○",
        // Анализ
        "liminf" to "lim inf", "limsup" to "lim sup", "arg" to "arg", "deg" to "deg",
        "dim" to "dim", "ker" to "ker", "coker" to "coker", "rank" to "rank",
        // Теория чисел
        "gcd" to "gcd", "lcm" to "lcm", "mod" to "mod"
    )

    private val mathbbMap = mapOf(
        "R" to "ℝ", "N" to "ℕ", "Z" to "ℤ", "Q" to "ℚ", "C" to "ℂ", "P" to "ℙ",
        "A" to "𝔸", "B" to "𝔹", "D" to "𝔻", "E" to "𝔼", "F" to "𝔽", "G" to "𝔾",
        "H" to "ℍ", "I" to "𝕀", "J" to "𝕁", "K" to "𝕂", "L" to "𝕃", "M" to "𝕄",
        "O" to "𝕆", "S" to "𝕊", "T" to "𝕋", "U" to "𝕌", "V" to "𝕍", "W" to "𝕎",
        "X" to "𝕏", "Y" to "𝕐"
    )

    fun mapCommand(cmd: String): String? = map[cmd] ?: additionalMap[cmd]

    fun mapMathbb(cmd: String): String? = mathbbMap[cmd]
} 