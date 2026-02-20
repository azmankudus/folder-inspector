import { configs, mockFiles as files, theme } from "../store";
import Search from "../components/Search";

export default function SearchPage() {
  return (
    <Search
      configs={configs}
      files={files}
      theme={theme}
    />
  );
}
