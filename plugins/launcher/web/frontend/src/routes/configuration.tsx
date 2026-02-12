import { configs, handleAddConfig, theme } from "../store";
import Configuration from "../components/Configuration";

export default function ConfigurationPage() {
  return (
    <Configuration
      configs={configs}
      onAdd={handleAddConfig}
      theme={theme}
    />
  );
}
