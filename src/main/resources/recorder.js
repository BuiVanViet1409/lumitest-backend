/**
 * DOM Analyzer & Web Recorder
 * Injected into the browser by RecorderService
 */

class SelectorGenerator {
  static generate(element) {
    if (!element || element.nodeType !== Node.ELEMENT_NODE) return null;

    // 1. data-testid
    const testId = element.getAttribute("data-testid");
    if (testId) return `[data-testid="${testId}"]`;

    // 2. id
    const id = element.getAttribute("id");
    if (id && !/\d{4,}/.test(id)) return `#${CSS.escape(id)}`;

    // 3. name
    const name = element.getAttribute("name");
    if (name) return `[name="${CSS.escape(name)}"]`;

    // 4. aria-label
    const ariaLabel = element.getAttribute("aria-label");
    if (ariaLabel) return `[aria-label="${CSS.escape(ariaLabel)}"]`;

    // 5. placeholder
    const placeholder = element.getAttribute("placeholder");
    if (placeholder) return `[placeholder="${CSS.escape(placeholder)}"]`;

    // 6. visible text (for buttons/links)
    const text = element.innerText?.trim();
    if (
      text &&
      text.length > 0 &&
      text.length < 50 &&
      ["button", "a"].includes(element.tagName.toLowerCase())
    ) {
      return `${element.tagName.toLowerCase()}:has-text("${text}")`;
    }

    // 7. CSS path
    const cssPath = this._getCssPath(element);
    if (cssPath) return cssPath;

    // 8. XPath fallback
    return this._getXPath(element);
  }

  static _getCssPath(element) {
    if (element.tagName.toLowerCase() === "html") return "html";
    let path = [];
    let current = element;
    while (
      current &&
      current.nodeType === Node.ELEMENT_NODE &&
      current.tagName.toLowerCase() !== "html"
    ) {
      let selector = current.tagName.toLowerCase();
      if (current.id) {
        selector += `#${CSS.escape(current.id)}`;
        path.unshift(selector);
        break;
      } else {
        let sibling = current;
        let nth = 1;
        while ((sibling = sibling.previousElementSibling)) {
          if (sibling.tagName === current.tagName) nth++;
        }
        if (nth > 1 || current.nextElementSibling) {
          selector += `:nth-of-type(${nth})`;
        }
      }
      path.unshift(selector);
      current = current.parentNode;
    }
    return path.join(" > ");
  }

  static _getXPath(element) {
    if (element.id !== "") return `//*[@id="${element.id}"]`;
    if (element === document.body) return "/html/body";

    let ix = 0;
    const siblings = element.parentNode.childNodes;
    for (let i = 0; i < siblings.length; i++) {
      const sibling = siblings[i];
      if (sibling === element) {
        return (
          this._getXPath(element.parentNode) +
          "/" +
          element.tagName.toLowerCase() +
          "[" +
          (ix + 1) +
          "]"
        );
      }
      if (sibling.nodeType === 1 && sibling.tagName === element.tagName) ix++;
    }
  }
}

// Global recorder bindings
window.SelectorGenerator = SelectorGenerator;

document.addEventListener(
  "click",
  (e) => {
    const selector = window.SelectorGenerator.generate(e.target);
    if (!selector) return;

    // Optional: send context menu verify events by capturing right clicks or special keys
    if (e.altKey) {
      e.preventDefault();
      window.recorderPostEvent({
        action: "verify_text",
        selector: selector,
        value: e.target.innerText.trim(),
      });
      return;
    }

    window.recorderPostEvent({
      action: "click",
      selector: selector,
      tagName: e.target.tagName.toLowerCase(),
      text: e.target.innerText?.substring(0, 50),
    });
  },
  true,
);

document.addEventListener(
  "change",
  (e) => {
    const selector = window.SelectorGenerator.generate(e.target);
    if (!selector) return;
    const tagName = e.target.tagName.toLowerCase();
    const type = e.target.type;

    if (tagName === "input" && (type === "checkbox" || type === "radio")) {
      window.recorderPostEvent({
        action: "checkbox_toggle",
        selector: selector,
        value: e.target.checked.toString(),
      });
    } else if (tagName === "select") {
      window.recorderPostEvent({
        action: "select",
        selector: selector,
        value: e.target.value,
      });
    } else {
      window.recorderPostEvent({
        action: "type",
        selector: selector,
        value: e.target.value,
      });
    }
  },
  true,
);

document.addEventListener(
  "input",
  (e) => {
    if (
      e.target.tagName.toLowerCase() === "input" &&
      e.target.type === "file"
    ) {
      const selector = window.SelectorGenerator.generate(e.target);
      if (selector && e.target.files.length > 0) {
        window.recorderPostEvent({
          action: "file_upload",
          selector: selector,
          value: e.target.files[0].name, // Simplified
        });
      }
    }
  },
  true,
);
