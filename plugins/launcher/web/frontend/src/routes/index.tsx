import { createEffect, createSignal } from "solid-js";
import { configs, mockFiles as files, theme, history } from "../store";
import Dashboard from "../components/Dashboard";

export default function Home() {
  // Using global store directly. In a real app you might want to fetch data here.
  return (
    <Dashboard
      files={files}
      configs={configs}
      theme={theme}
    />
  );
}
