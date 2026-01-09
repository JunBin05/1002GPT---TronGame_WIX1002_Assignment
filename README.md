# 🏍️ FOP Tron: Light Cycle Arena

![Java](https://img.shields.io/badge/Language-Java-orange)
![Course](https://img.shields.io/badge/Course-WIX1002-blue)
![Status](https://img.shields.io/badge/Status-Development-green)

> "I fight for the users."

## 📝 Introduction

**FOP Tron** is a 2D interactive arena game inspired by the *Tron: Legacy (2010)* and *Tron (1982)* universe. Designed for the **WIX1002 Fundamentals of Programming** assignment, this project combines strategic gameplay with narrative storytelling.

Players control a futuristic Light Cycle leaving behind a glowing energy trail (jetwall) on the Grid. The objective is to outmaneuver opponents, forcing them to collide with walls, jetwalls, or each other.

## ✨ Features

### Core Mechanics (Basic Requirements)
* **Arena Design:** A $40 \times 40$ grid-based battlefield where movement and collisions are critical.
* **Playable Characters:** Choose between **Tron** (Balanced/Defender) and **Kevin Flynn** (High Speed/Creator), with attributes loaded dynamically via File I/O.
* **Leveling System:** Earn XP to level up (max Level 99), increasing speed, handling, and unlocking new abilities.
* **Smart AI Enemies:** Four unique enemy types with distinct behaviors ranging from "Erratic" to "Impossible".
* **Combat System:**
    * **Jetwalls:** Create barriers to trap enemies.
    * **Identity Discs:** Throw and recapture discs to damage opponents (-1 Life).
* **Technical Implementation:** Utilizes Object-Oriented Programming (Abstraction, Inheritance) and robust File I/O handling.

### Extra Features (Bonus)
* **💾 Save/Load System:** Save your current progress (Level, XP) and resume later.
* **🏆 Leaderboard:** Tracks top player rankings.
* **📖 Story Mode:** Unlocks narrative cutscenes and chapters as you progress through the Grid.
* **🎲 Random Arena Generator:** Procedurally generated maps to ensure no two matches are the same.

## 🎮 How to Play

### Controls
Movement is controlled using the WASD keys:
* **W**: Move Up
* **S**: Move Down
* **A**: Move Left
* **D**: Move Right

### Rules of the Grid
1.  **Survival:** Do not crash into the arena boundaries or Jetwalls.
    * *Penalty:* -0.5 Lives for wall collision.
    * *Penalty:* Instant Death for falling off an open grid.
2.  **Combat:** Avoid enemy discs.
    * *Penalty:* -1 Life if hit.
3.  **Victory:** Be the last Light Cycle standing or derezz all enemies to win the round and earn XP.

## 🤖 Characters & Enemies

### Playable Heroes
| Character | Color | Traits | Description |
| :--- | :--- | :--- | :--- |
| **Tron** | Blue | Moderate Speed, Balanced | The original defender of the Grid. |
| **Kevin** | White | Very High Speed, Smooth | The creator; stable and resilient. |

### The Opposition
| Enemy | Difficulty | Color | Behavior |
| :--- | :--- | :--- | :--- |
| **Clu** | Impossible | Gold | Very fast, strategic, non-deterministic decisions. |
| **Rinzler** | Hard | Red | Silent hunter, tactical, adaptive. |
| **Sark** | Medium | Yellow | Predictable, standard path prediction. |
| **Koura** | Easy | Green | Weak, random movement patterns. |

## 👥 Team Members

* **Member 1 :LIM JUN BIN** - (25006412)
* **Member 2 :TEY YONG ZHUN** - (25006379)
* **Member 3 :EDRIAN TAH KAH HENG** - (25006656)
* **Member 4 :CHEW KEAN HONG** - (25006697)
* **Member 5 :LEE WUEY YONG** - (25005996)

## 📜 Credits & Acknowledgments

* **Course:** WIX1002 Fundamentals of Programming, Universiti Malaya.
* **Assignment:** Topic 1 - Tron Legacy.
* **Instructor:** Mr. Mohammad Shahid Akhtar.
* **Inspiration:** Tron (1982) & Tron: Legacy (2010).
* *Some content adapted from FOP Valley by Ng Zhi Yang.*.

---
*Created: W3 to W13*
