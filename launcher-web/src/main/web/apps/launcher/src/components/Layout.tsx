import { Component, JSX, onMount } from 'solid-js';
import { Layout as SharedLayout, NavItem } from '@folder-inspector/ui';
import { toggleTheme, theme, username, setMockFiles, mockFiles } from '../store';
import { generateMockFiles } from '../constants';

interface LayoutProps {
  children?: JSX.Element;
}

const Layout: Component<LayoutProps> = (props) => {
  const navItems: NavItem[] = [
    { label: 'Dashboard', href: '/' },
    { label: 'Search', href: '/search' },
    { label: 'History', href: '/history' },
    { label: 'Service', href: '/service' },
    { label: 'Configuration', href: '/configuration' },
    { label: 'Documentation', href: '/documentation' },
    { label: 'Help', href: '/help' },
  ];

  onMount(() => {
    if (mockFiles().length === 0) {
      setMockFiles(generateMockFiles(250));
    }
  });

  return (
    <SharedLayout
      theme={theme()}
      toggleTheme={toggleTheme}
      username={username()}
      navItems={navItems}
    >
      {props.children}
    </SharedLayout>
  );
};

export default Layout;
