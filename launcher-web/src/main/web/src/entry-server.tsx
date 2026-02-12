// @refresh reload
import { createHandler, StartServer } from "@solidjs/start/server";

export default createHandler(() => (
  <StartServer
    document={({ assets, children, scripts }) => (
      <html lang="en">
        <head>
          <script dangerouslySetInnerHTML={{
            __html: `
            (function() {
              try {
                const storedTheme = localStorage.getItem('theme');
                if ((storedTheme && storedTheme.toLowerCase() === 'dark') || (!storedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
                  document.documentElement.classList.add('dark');
                  document.documentElement.style.backgroundColor = '#0f172a';
                  document.documentElement.style.color = '#e2e8f0';
                }
              } catch (e) {}
            })();
          `}} />
          <style>
            {`
              html.dark { background-color: #0f172a !important; color: #e2e8f0 !important; }
              html.dark body { background-color: #0f172a !important; color: #e2e8f0 !important; }
            `}
          </style>
          <meta name="color-scheme" content="light dark" />
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <link rel="icon" href="/favicon.ico" />
          {assets}
        </head>
        <body>
          <div id="app">{children}</div>
          {scripts}
        </body>
      </html>
    )}
  />
));
