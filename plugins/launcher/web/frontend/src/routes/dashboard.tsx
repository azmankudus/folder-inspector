import { configs, mockFiles as files, theme } from "../store";
import Dashboard from "../components/Dashboard";

export default function DashboardPage() {
  return (
    <Dashboard
      files={files}
      configs={configs}
      theme={theme}
    />
  );
}
