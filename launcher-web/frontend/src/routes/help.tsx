import { Component, createSignal } from 'solid-js';
import { isDark } from '../store';

const Help: Component = () => {
  const [activeFaq, setActiveFaq] = createSignal<number | null>(null);

  const faqs = [
    {
      id: 1,
      question: "How do I scan a specific folder?",
      answer: "Navigate to the Dashboard and click the 'Scan New Folder' button in the top right corner. Select your directory from the dialog."
    },
    {
      id: 2,
      question: "Can I export the file report?",
      answer: "Yes, you can export reports in CSV, JSON, or PDF formats from the results view after a scan is completed."
    },
    {
      id: 3,
      question: "Is there a dark mode?",
      answer: "Yes! You can toggle between light and dark modes using the sun/moon icon in the top navigation bar."
    },
    {
      id: 4,
      question: "How do I delete files?",
      answer: "For safety reasons, Folder Inspector currently only allows you to view files. Deletion features are planned for a future update."
    }
  ];

  const toggleFaq = (id: number) => {
    setActiveFaq(activeFaq() === id ? null : id);
  };

  return (
    <div class="space-y-8 animate-in fade-in duration-500">
      {/* Header Section */}
      <div class="p-8 rounded-2xl border bg-white border-slate-200 shadow-sm dark:bg-slate-900 dark:border-slate-800">
        <div class="flex items-center gap-4 mb-4">
          <div class="p-3 rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-900/30 dark:text-indigo-400">
            <i class="fa-solid fa-circle-question text-2xl p-1"></i>
          </div>
          <div>
            <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Help Center</h1>
            <p class="text-slate-600 dark:text-slate-400">Frequently asked questions and support</p>
          </div>
        </div>
      </div>

      {/* FAQs */}
      <div class="max-w-3xl mx-auto">
        <h2 class="text-xl font-bold mb-6 text-slate-900 dark:text-white">Frequently Asked Questions</h2>
        <div class="space-y-4">
          {faqs.map((faq) => (
            <div class={`rounded-xl border overflow-hidden transition-all duration-200 bg-white border-slate-200 dark:bg-slate-900 dark:border-slate-800`}>
              <button
                onClick={() => toggleFaq(faq.id)}
                class="w-full px-6 py-4 flex items-center justify-between text-left font-medium transition-colors text-slate-800 hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800"
              >
                <span>{faq.question}</span>
                <i class={`fa-solid fa-chevron-down transition-transform duration-200 ${activeFaq() === faq.id ? 'rotate-180' : ''} text-slate-500 dark:text-slate-400 p-1`}></i>
              </button>
              <div
                class={`transition-all duration-200 overflow-hidden ${activeFaq() === faq.id ? 'max-h-40 opacity-100' : 'max-h-0 opacity-0'}`}
              >
                <div class="px-6 pb-4 pt-0 text-slate-600 dark:text-slate-400">
                  {faq.answer}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Support Contact */}
      <div class="mt-12 p-8 rounded-2xl text-center border bg-slate-50 border-slate-200 dark:bg-slate-900 dark:border-slate-800">
        <h3 class="text-lg font-bold mb-2 text-slate-900 dark:text-white">Still need help?</h3>
        <p class="mb-6 text-slate-600 dark:text-slate-400">Our support team is available 24/7 to assist you with any issues.</p>
        <button class="px-6 py-2 bg-gradient-to-br from-red-600 to-slate-900 text-white rounded-lg font-medium transition-colors shadow-sm hover:shadow-md">
          Contact Support
        </button>
      </div>
    </div >
  );
};

export default Help;
