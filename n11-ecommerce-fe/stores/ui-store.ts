"use client";

import { create } from "zustand";

type UiState = {
  optimisticCartCount: number;
  language: "tr" | "en";
  bumpCart: (quantity?: number) => void;
  resetCart: () => void;
  setLanguage: (language: "tr" | "en") => void;
};

export const useUiStore = create<UiState>((set) => ({
  optimisticCartCount: 0,
  language: "tr",
  bumpCart: (quantity = 1) => set((state) => ({ optimisticCartCount: state.optimisticCartCount + quantity })),
  resetCart: () => set({ optimisticCartCount: 0 }),
  setLanguage: (language) => set({ language }),
}));
