function BarDragger(e) {
    this.handles = [e], this.bindHandles()
}

function CPAdsinsertAd(e) {
    var t = e.ads[0];
    if (t.statlink) {
        var n = $("<a />", {
            href: t.statlink,
            target: "_blank",
            rel: "noopener",
            text: t.description
        });
        if (t.pixel)
            for (var s = t.pixel.split("||"), i = 0 | Math.round(Date.now() / 1e4), o = 0; o < s.length; o++) {
                var r = document.createElement("img");
                r.src = s[o].replace("[timestamp]", i), r.border = "0", r.height = "1", r.width = "1", r.style.display = "none", n.append(r)
            }
        $("#bsa-footer").append(n)
    }
}
var Love = {
    _saveLoveToDBFunc: null,
    THROTTLE_DELAY: 500,
    init: function() {
        _.extend(this, AJAXUtil), this.user = window.__user, this._bindToDOM(), this._createSaveLoveToDBFunc()
    },
    _createSaveLoveToDBFunc: function() {
        this._saveLoveToDBFunc = _.throttle(this._saveLoveChange, this.THROTTLE_DELAY, {
            leading: !1,
            trailing: !0
        })
    },
    _bindToDOM: function() {
        $("body").on("click", "a.loves, button.loves, span.loves", $.proxy(this._onHeartClick, this))
    },
    _onHeartClick: function(e) {
        if (e.preventDefault(), this._userNeedsToLogin()) return this._redirectToLogin();
        var t = $(e.target).closest(".loves");
        this._isOwned(t) || this._heartItem(t)
    },
    _userNeedsToLogin: function() {
        return 1 == this.user.id
    },
    _redirectToLogin: function() {
        window.location = "/login?type=love"
    },
    _isOwned: function(e) {
        return e.data("owned")
    },
    _heartItem: function(e) {
        var t = this._getNextLevel(e);
        this._showAsHearted(e, t), this._saveLoveToDBFunc(e.data("item"), e.data("hashid"), t)
    },
    _getNextLevel: function(e) {
        if (e.hasClass("comment-heart")) return this._isCommentedLoved(e) ? 0 : 1;
        var t = this._findTextLoveLevel(e.attr("class"));
        return this._nextLoveLevel(t)
    },
    _isCommentedLoved: function(e) {
        return e.hasClass("love")
    },
    _findTextLoveLevel: function(e) {
        var t = e.match(/loved-(\d)/);
        return t ? 1 * t[1] : 0
    },
    _nextLoveLevel: function(e) {
        return 3 === e ? 0 : e + 1
    },
    _showAsHearted: function(e, t) {
        this._isHeartingComment(e) ? this._showCommentHeartAsHearted(e, t) : this._showStandardHeartAsHearted(e, t)
    },
    _isHeartingComment: function(e) {
        return e.hasClass("comment-heart")
    },
    _showCommentHeartAsHearted: function(e, t) {
        t > 0 ? e.addClass("love loved-1") : e.removeClass("love loved-1"), this._updateLoveCount(e, t)
    },
    _showStandardHeartAsHearted: function(e, t) {
        var n = e.data("hashid"),
            s = $("[data-hashid='" + n + "']");
        $.each(s, function(e, n) {
            var s = $(n);
            if (!s.hasClass("count")) {
                var i = s.hasClass("single-stat") ? s : $(".heart-button");
                i.removeClass("loved-1 loved-2 loved-3 loved-0").attr("aria-pressed", !1).addClass("loved-" + t), 0 != t && i.attr("aria-pressed", !0), Love._updateLoveCount(i, t)
            }
        })
    },
    _updateLoveCount: function(e, t) {
        var n = e.find("span.count"),
            s = this._getValueToAddToCount(t);
        n.html(this._getLoveCount(n, s))
    },
    _getValueToAddToCount: function(e) {
        return 0 === e ? -1 : 1 === e ? 1 : 0
    },
    _getLoveCount: function(e, t) {
        var n = e.html();
        if (n) {
            var s = 1 * n.replace(/[,\. ]/g, ""),
                i = isNaN(s) ? 0 : s;
            return i += t, i > 0 ? this._delimit(i) : ""
        }
        return ""
    },
    _delimit: function(e, t) {
        return t = t || ",", e.toString().replace(/\B(?=(\d{3})+(?!\d))/g, t)
    },
    _saveLoveChange: function(e, t, n) {
        var s = "/love/" + [e, t, n].join("/");
        this.post(s, {}, function() {
            this._doneSaveLoveChange(e)
        })
    },
    _doneSaveLoveChange: function(e) {
        "undefined" != typeof Hub && Hub.pub(e + "-hearted")
    }
};
Love.init();
var URLBuilder = {
        getHostURL: function(e, t, n) {
            var s = document.location,
                i = n ? "https:" : "",
                o = s.hostname,
                r = e ? window.__CPDATA.host_secure_subdomain : "";
            r = void 0 === typeof t ? r : t;
            var a = s.port ? ":" + s.port : "",
                c = "<%= protocol %>//<%= subdomain %><%= host %><%= port %>";
            return _.template(c, {
                protocol: i,
                subdomain: r,
                host: o,
                port: a
            })
        },
        getViewURL: function(e, t, n, s) {
            return this.getViewURLSimple(e, t.base_url, n.getActiveSlugHash(), s)
        },
        getItemViewURL: function(e, t, n, s, i) {
            return this.getItemViewURLSimple(e, t.base_url, n, s.getActiveSlugHash(), i)
        },
        getViewURLSimple: function(e, t, n, s, i) {
            var o = URLBuilder.getHostURL(s, void 0, i);
            return o += t + "/", o += e + "/", o += n + "/"
        },
        getItemViewURLSimple: function(e, t, n, s, i, o) {
            var r = URLBuilder.getHostURL(i, void 0, o);
            return r += t + "/", r += n + "/", r += e + "/", r += s + "/"
        },
        SHORT_HOST: "https://cdpn.io",
        getShortViewURL: function(e, t) {
            return e ? [this.SHORT_HOST, e, t.getActiveSlugHash()].join("/") : [this.SHORT_HOST, t.getActiveSlugHash()].join("/")
        }
    },
    LocalDataLoader = {
        latestObject: function(e, t, n) {
            if (!n) return e;
            if ("undefined" === n) return e;
            var s = $.parseJSON(n);
            return this._localStorageObjNewer(e, t, s) ? s : e
        },
        _localStorageObjNewer: function(e, t, n) {
            return this._getComparableClientTime(n, t) > t
        },
        _getComparableClientTime: function(e, t) {
            var n = e.last_updated + "",
                s = t + "",
                i = n.substr(0, s.length);
            return +i
        }
    };
$.fn.serializeObject = function() {
    var e = {},
        t = this.serializeArray();
    return $.each(t, function() {
        e[this.name] ? (e[this.name].push || (e[this.name] = [e[this.name]]), e[this.name].push(this.value || "")) : e[this.name] = this.value || ""
    }), e
};
var User = Class.extend({
    recentScripts: {},
    init: function() {
        _.extend(this, __user)
    },
    isUserLoggedIn: function() {
        return +this.id > 1
    },
    saveAccomplishment: function(e) {
        AJAXUtil.post("/accomplishments", {
            name: e
        })
    },
    isAnon: function() {
        return +this.id < 2
    },
    updateUser: function(e) {
        _.extend(this, e)
    },
    addRecentSelectedScript: function(e) {
        this.recentScripts[e.name] = e
    }
});
window.Analyze = {
    errorStorage: {
        html: {
            lintErrorWidgets: []
        },
        css: {
            lintErrorWidgets: []
        },
        js: {
            lintErrorWidgets: []
        }
    },
    ANALYZE_LAMBDA_URL: "https://gmqxehjo54.execute-api.us-west-2.amazonaws.com/production/analyze",
    html: function() {
        var e = !!("srcdoc" in document.createElement("iframe"));
        if (!e) return void $.showMessage("Sorry, we can't analyze safely because your browser doesn't support `srcdoc` ... :(", 1500);
        CP.htmlEditor.showOriginalCode(), this.clearAllErrors("html");
        for (var t = ["<script src='https://cdnjs.cloudflare.com/ajax/libs/html-inspector/0.8.2/html-inspector.js'></script>", "<script>function inspect() { HTMLInspector.inspect({domRoot: document.body,excludeRules: ['unnecessary-elements'],onComplete: function(errors) { var cleanErrors = []; for( var i = 0; i < errors.length; i++) { cleanErrors.push({message: errors[i].message, rule: errors[i].rule}) }; parent.postMessage({type: 'html-errors', errors: cleanErrors}, '*')}});};</script>", "<script>window.onload = function() { inspect(); }</script>", "<style>", this._getCodeToCheck("css"), "</style>", this._getCodeToCheck("html")], n = "", s = 0; s < t.length; s++) n += t[s];
        var i = document.createElement("iframe");
        i.sandbox = "allow-forms allow-scripts", i.className = "hidden-iframe", window.addEventListener("message", Analyze.HTMLErrorPostmessageReceiver, !1), document.body.appendChild(i), i.srcdoc = n
    },
    HTMLErrorPostmessageReceiver: function(e) {
        if (e.data && "html-errors" === e.data.type) {
            window.removeEventListener("message", Analyze.HTMLErrorPostmessageReceiver, !1);
            var t = document.querySelector(".hidden-iframe");
            t.parentNode.removeChild(t), Analyze.showHTMLErrors(e.data.errors)
        }
    },
    showHTMLErrors: function(e) {
        if (e.length > 0) {
            var t = _.clone(CP.pen.html);
            "none" !== CP.pen.html_pre_processor && (CP.htmlEditor.editor.setOption("readOnly", !0), $("#box-html").addClass("view-preproc-errors"), CP.htmlEditor.setValue(CP.penProcessor.getProcessed("html")), CP.htmlEditor.unbindOnChange(), this.showUnprocessedCodeButton("html"), $("#viewsource-html").one("click", function() {
                $("#box-html").removeClass("view-preproc-errors"), Analyze.clearAllErrors("html"), CP.htmlEditor.setValue(t), CP.htmlEditor.bindToOnChange(), CP.htmlEditor.editor.setOption("readOnly", !1)
            }));
            for (var n = "<h4 style='color: white;''>HTML Inspector warnings:</h4><ul class='html-errors'>", s = !1, i = {}, o = [], r = 0; r < e.length; r++)
                if ("unused-classes" === e[r].rule) {
                    var a = e[r].message.split("'")[1];
                    i[a] ? i[a]++ : (i[a] = 1, s = !0)
                } else o.push(e[r]);
            var c = {
                    message: "The classes "
                },
                u = "";
            if (s) {
                var l = Object.keys(i);
                for (r = 0; r < l.length; r++) u = u + l[r] + " (" + i[l[r]] + ") ";
                c.message += u + "are used in the HTML but not found in any stylesheet.", this._getCSSLibs() > 0 && (c.message += " Since you have external stylesheets, you can probably ignore this."), o.unshift(c)
            }
            for (r = 0; r < o.length; r++) n += "<li>" + _.escape(o[r].message) + "</li>";
            n += "</ul>";
            var d = 0;
            Analyze.errorStorage.html.lintErrorWidgets.push(CP.htmlEditor.editor.addLineWidget(d, $("<div class='inline-editor-error yellow'>" + n + "</div>")[0], {
                coverGutter: !0,
                noHScroll: !0,
                above: !0
            })), this.addClearErrorsButton("html")
        } else $.showMessage("HTML Inspector found no errors. Rejoice!", 1500)
    },
    css: function() {
        this.clearAllErrors("css");
        var e = this._getCodeToCheck("css");
        $.post(this.ANALYZE_LAMBDA_URL, {
            type: "css",
            content: e
        }, $.proxy(this._cssAnalyzeCallback, this))
    },
    _cssAnalyzeCallback: function(e) {
        if (0 === e.length) $.showMessage("No errors found! You're good.", 1500);
        else {
            CP.cssEditor.showOriginalCode(), "none" !== CP.pen.css_pre_processor && (CP.cssEditor.editor.setOption("readOnly", !0), $("#box-css").addClass("view-preproc-errors"), CP.cssEditor.setValue(CP.penProcessor.getProcessed("css")), CP.cssEditor.unbindOnChange(), this.showUnprocessedCodeButton("css"), $("#viewsource-css").one("click", function() {
                $("#box-css").removeClass("view-preproc-errors"), Analyze.clearAllErrors("css"), CP.cssEditor.setValue(CP.pen.css), CP.cssEditor.bindToOnChange(), CP.cssEditor.editor.setOption("readOnly", !1)
            }));
            for (var t = 0; t < e.length; t++) {
                var n = e[t].line - 1;
                0 === n && (n = 1), Analyze.errorStorage.css.lintErrorWidgets.push(CP.cssEditor.editor.addLineWidget(n, $("<div class='inline-editor-error'>" + _.escape(e[t].message) + " (<a target='_blank' href='https://google.com/search?q=CSS Lint " + _.escape(e[t].message) + "'>Google it</a>)</div>")[0], {
                    coverGutter: !0,
                    noHScroll: !0
                }))
            }
            var s = e[0].line - 1;
            s = s > -1 ? s : 0, CP.cssEditor.editor.scrollIntoView(s), this.addClearErrorsButton("css")
        }
    },
    js: function() {
        this.clearAllErrors("js");
        var e = this._getCodeToCheck("js");
        $.post(this.ANALYZE_LAMBDA_URL, {
            type: "js",
            content: e
        }, $.proxy(this._jsAnalyzeCallback, this))
    },
    _jsAnalyzeCallback: function(e) {
        if (0 === e.length) $.showMessage("JS Hint found no errors! You're good.", 1500);
        else {
            CP.jsEditor.showOriginalCode(), "none" !== CP.pen.js_pre_processor && (CP.jsEditor.editor.setOption("readOnly", !0), $("#box-js").addClass("view-preproc-errors"), CP.jsEditor.setValue(CP.penProcessor.getProcessed("js")), CP.jsEditor.unbindOnChange(), this.showUnprocessedCodeButton("js"), $("#viewsource-js").one("click", function() {
                $("#box-js").removeClass("view-preproc-errors"), Analyze.clearAllErrors("js"), CP.jsEditor.setValue(CP.pen.js), CP.jsEditor.bindToOnChange(), CP.jsEditor.editor.setOption("readOnly", !1)
            }));
            for (var t = 0, n = 0; n < e.length; n++) {
                if (null === e[n]) return;
                t = e[n].line - 1, Analyze.errorStorage.js.lintErrorWidgets.push(CP.jsEditor.editor.addLineWidget(t, $("<div class='inline-editor-error'>" + e[n].reason + " (<a target='_blank' href='https://google.com/search?q=JS Hint " + e[n].reason + "'>Google it</a>)</div>")[0], {
                    coverGutter: !0,
                    noHScroll: !0
                }))
            }
            var s = e[0].line - 1;
            s = s > -1 ? s : 0, CP.jsEditor.editor.scrollIntoView(s), this.addClearErrorsButton("js")
        }
    },
    _getCodeToCheck: function(e) {
        var t = "";
        return t = CP.pen[e + "_pre_processor" == "none"] || "js" === e ? CP.pen[e] : CP.penProcessor.getProcessed(e), "" === t && (t = " "), t
    },
    _getCSSLibs: function() {
        for (var e = 0, t = CP.pen.getResourcesByType("css"), n = 0; n < t.length; n++) t[n].url && "" !== t[n].url && e++;
        return e
    },
    clearAllErrors: function(e) {
        for (var t = Analyze.errorStorage[e].lintErrorWidgets, n = 0, s = t.length; n < s; n++) t[n].clear();
        $("#clear-" + e + "-errors").remove(), Analyze.errorStorage[e].lintErrorWidgets = []
    },
    showUnprocessedCodeButton: function(e) {
        var t = {
            ui: {
                editorViewSource: {}
            }
        };
        CP.ui.editorViewSource[e] = !0, Hub.pub("ui-change", t);
        var n = $(".view-compiled-button[data-type='" + e + "']").closest(".box").data("preprocessor");
        $(".view-compiled-button[data-type='" + e + "']").text("View Uncompiled " + n), $(".view-compiled-button[data-type='" + e + "']").addClass("active")
    },
    addClearErrorsButton: function(e) {
        if (this._hasErrors(e)) {
            var t = this._getClearErrorsButtonHTML(e);
            $(this._getErrorsButtonElID(e)).parent().after(t)
        }
    },
    _hasErrors: function(e) {
        return this.errorStorage[e].lintErrorWidgets.length > 0
    },
    _getClearErrorsButtonHTML: function(e) {
        var t = e.toUpperCase(),
            n = {
                type: e,
                upCaseType: t
            };
        return _.template(this._getClearErrorsButtonTmpl(), n)
    },
    _getClearErrorsButtonTmpl: function() {
        return "<li><a href='#0' class='clear-<%= type %>-errors' id='clear-<%= type %>-errors'>Clear <%= upCaseType %> Errors</a></li>"
    },
    _getErrorsButtonElID: function(e) {
        return "#analyze-" + e
    }
};
var CodeEditorAnalyze = Class.extend({
        clearingShortcut: !1,
        init: function() {
            this.bindUIActions()
        },
        bindUIActions: function() {
            $("#analyze-html")._on("click", this.analyzeHTML, this), $("#analyze-css")._on("click", this.analyzeCSS, this), $("#analyze-js")._on("click", this.analyzeJS, this), $(document).on("click", "#clear-html-errors", $.proxy(this.clearHTMLErrors, this)).on("click", "#clear-css-errors", $.proxy(this.clearCSSErrors, this)).on("click", "#clear-js-errors", $.proxy(this.clearJSErrors, this))
        },
        enableClearingShortcut: function() {
            Keytrap.bind("comctrl+shift+8", function() {
                Analyze.clearAllErrors("html"), Analyze.clearAllErrors("css"), Analyze.clearAllErrors("js")
            }, !0), this.clearingShortcut = !0
        },
        analyzeHTML: function() {
            this._hidePanesOnTopOfEditor();
            var e = this;
            Analyze.html(), Hub.pub("popup-close"), e.clearingShortcut === !1 && e.enableClearingShortcut()
        },
        analyzeCSS: function() {
            this._hidePanesOnTopOfEditor();
            var e = this;
            Analyze.css(), e.clearingShortcut === !1 && e.enableClearingShortcut(), Hub.pub("popup-close")
        },
        analyzeJS: function() {
            this._hidePanesOnTopOfEditor();
            var e = this;
            Analyze.js(), e.clearingShortcut === !1 && e.enableClearingShortcut(), Hub.pub("popup-close")
        },
        clearHTMLErrors: function(e) {
            e.preventDefault(), Analyze.clearAllErrors("html"), this._hidePanesOnTopOfEditor()
        },
        clearCSSErrors: function(e) {
            e.preventDefault(), Analyze.clearAllErrors("css"), this._hidePanesOnTopOfEditor()
        },
        clearJSErrors: function(e) {
            e.preventDefault(), Analyze.clearAllErrors("js"), this._hidePanesOnTopOfEditor()
        },
        _hidePanesOnTopOfEditor: function() {
            $.hideMessage(), Hub.pub("popup-close")
        }
    }),
    CMEditorSettings = {
        getDefaultEditorConfig: function(e, t, n) {
            return {
                value: e,
                showCursorWhenSelecting: !0,
                cursorScrollMargin: 30,
                scrollbarStyle: this._getScrollbars(n),
                tabSize: this.getTabSize(t),
                indentUnit: this.getTabSize(t),
                indentWithTabs: this.getIndentWithTabs(t),
                lineNumbers: t.line_numbers,
                matchBrackets: t.match_brackets,
                matchTags: t.match_brackets,
                autocomplete: t.autocomplete,
                autoCloseBrackets: t.match_brackets,
                lineWrapping: t.line_wrapping,
                gutters: this._getGutters(t),
                foldGutter: t.code_folding,
                scrollPastEnd: !0,
                emmet_active: t.emmet_active,
                emmet: this.getEmmetSnippets(t),
                markTagPairs: !0,
                autoRenameTags: !0
            }
        },
        getPostEditorConfig: function(e) {
            return {
                showCursorWhenSelecting: !0,
                tabSize: this.getTabSize(e),
                indentUnit: this.getTabSize(e),
                indentWithTabs: this.getIndentWithTabs(e),
                matchBrackets: e.match_brackets,
                autoCloseBrackets: e.match_brackets,
                lineWrapping: !0
            }
        },
        _getScrollbars: function(e) {
            return e ? null : "simple"
        },
        getTabSize: function(e) {
            return parseInt(e.tab_size, 10)
        },
        getIndentWithTabs: function(e) {
            return "tabs" === e.indent_with
        },
        getEmmetSnippets: function(e) {
            var t = e.snippets;
            if (!t) return {
                globals: {
                    markup: {
                        snippets: {}
                    },
                    stylesheet: {
                        snippets: {}
                    }
                }
            };
            t.markupSnippets = this.unescapeSnippet(t.markupSnippets), t.stylesheetSnippets = this.unescapeSnippet(t.stylesheetSnippets);
            var n = {
                globals: {
                    markup: {
                        snippets: t.markupSnippets
                    },
                    stylesheet: {
                        snippets: t.stylesheetSnippets
                    }
                }
            };
            return n
        },
        unescapeSnippet: function(e) {
            for (var t = e, n = Object.keys(t), s = 0; s < n.length; s++) t[n[s]] = t[n[s]].replace(/\\n/g, "\n").replace(/\\t/g, "\t");
            return t
        },
        _getGutters: function(e) {
            return e.code_folding === !0 ? ["CodeMirror-linenumbers", "CodeMirror-foldgutter"] : [""]
        }
    },
    BaseEditorKeyBindingsMixin = {
        _onCmUpdateKeyBindings: function(e) {
            switch (e.key_bindings) {
                case "vim":
                    this.editor.setOption("vimMode", !0);
                    break;
                case "subl":
                    this.editor.setOption("keyMap", "sublime");
                    break;
                default:
                    this.editor.setOption("keyMap", "extendedBase")
            }
        },
        _canAdjustRenderedLine: function(e) {
            return ("undefined" == typeof TypesUtil || "html" === TypesUtil.cmModeToType(e.getOption("mode"))) && ("undefined" != typeof CP.pen && "tabs" !== CP.pen.editor_settings.indent_with)
        },
        _indentWrappedLines: function() {
            var e = this.editor.defaultCharWidth(),
                t = 2,
                n = this;
            this.editor.on("renderLine", function(s, i, o) {
                if (n._canAdjustRenderedLine(s)) {
                    var r = s.getOption("tabSize"),
                        a = CodeMirror.countColumn(i.text, null, r),
                        c = a * e;
                    o.style.textIndent = "-" + c + "px", o.style.paddingLeft = t + c + "px"
                }
            })
        }
    },
    BaseEditor = Class.extend({
        editor: "",
        _viewingSource: !1,
        _canDrive: !0,
        type: "",
        value: "",
        init: function(e, t) {
            this.type = e, this.value = t, this.pageType = __pageType, this.mobile = __mobile, _.extend(this, BaseEditorViewSourceMixin), _.extend(this, BaseEditorKeyBindingsMixin), this._baseBindToHub(), this._buildEditor()
        },
        _baseBindToHub: function() {
            Hub.sub("page-loading-done", $.proxy(this._onPageLoadingDone, this)), Hub.sub("editor-refresh", $.proxy(this.refresh, this)), Hub.sub("key", $.proxy(this._onKey, this)), Hub.sub("cm-update-keybindings", $.proxy(this._onCMUpdateKeybindingsEvent, this)), Hub.sub("pen-change", $.proxy(this._onBasePenChange, this)), "professor" === window.__pageType && this._baseBindToHubProfessorMode()
        },
        _baseBindToHubProfessorMode: function() {
            Hub.sub("ui-disable", $.proxy(this._disableUserFromDriving, this)), Hub.sub("ui-enable", $.proxy(this._enableUserToDrive, this))
        },
        _onCMUpdateKeybindingsEvent: function() {
            this._onCmUpdateKeyBindings(window.__item.editor_settings)
        },
        _onKey: function(e, t) {
            "esc" === t.key && (this.editor.execCommand("clearSearch"), this.runRefresh(200))
        },
        _onBasePenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.editor_settings.indent_with") && this.editor.setOption("indentWithTabs", CMEditorSettings.getIndentWithTabs(t.pen.editor_settings)), ObjectUtil.hasNestedValue(t, "pen.editor_settings.tab_size") && (this.editor.setOption("tabSize", CMEditorSettings.getTabSize(t.pen.editor_settings)), this.editor.setOption("indentUnit", CMEditorSettings.getTabSize(t.pen.editor_settings)))
        },
        _onPageLoadingDone: function() {
            this._indentWrappedLines(), this.runRefresh(200)
        },
        _buildEditor: function() {
            this.editor = this._buildCMEditor(this._getEditorConfig());
            var e = this;
            this._throttledRefresh = _.throttle(function() {
                e.editor.refresh()
            }, 300), this._setMode(), this._bindToHub(), this._setEditorTypeSpecificOptions(this._getEditorConfig()), this.bindToOnChange(), this._bindAutoComplete(), this._syncEditorWithFirepad()
        },
        _syncEditorWithFirepad: function() {
            "collab" === window.__pageType && this._syncWithCollab(), "professor" === window.__pageType && this._syncWithProfessor()
        },
        _syncWithCollab: function() {
            var e = this;
            this.firepad = Firepad.fromCodeMirror(CP.collabRoom.getFirebaseEditorRef(this.type), this.editor, {
                richTextShortcuts: !1,
                richTextToolbar: !1,
                defaultText: null,
                userId: CP.collabRoom.rtData.user.id
            }), this.firepad.on("ready", function() {
                CP.collabRoom.onFirepadReady(e.type, e.firepad, e.value)
            }), this.firepad.on("synced", function(e) {
                CP.collabRoom.onFirepadSynced(e)
            })
        },
        _syncWithProfessor: function() {
            this.firepad = Firepad.fromCodeMirror(ProfessorRoom.getFirebaseEditorRef(this.type), this.editor, {
                richTextShortcuts: !1,
                richTextToolbar: !1,
                defaultText: this.value,
                userId: CP.user.session_hash,
                disablePublishEditorChanges: "professor" !== window.__rtData.user.role
            })
        },
        _getEditorConfig: function() {
            var e = _.extend(CMEditorSettings.getDefaultEditorConfig(this._getEditorConfigDefaultValue(), CP.pen.editor_settings, window.__mobile), this._getEditorTypeSpecificConfig());
            return e.extraKeys = {
                Tab: "respectfulTab",
                Enter: "emmetInsertLineBreak",
                "Shift-Cmd-A": "emmetWrapWithAbbreviation"
            }, "js" !== this.type && e.emmet_active === !0 && (e.extraKeys.Tab = function(e) {
                var t = e.execCommand("emmetExpandAbbreviation");
                t !== !0 && e.execCommand("respectfulTab")
            }), e
        },
        _getEditorConfigDefaultValue: function() {
            return "collab" === window.__pageType || "professor" === window.__pageType ? "" : this.value
        },
        _buildCMEditor: function(e) {
            var t = this._getTextAreaElementToReplaceWithCodeMirror();
            return CodeMirror(function(e) {
                t.parentNode.replaceChild(e, t)
            }, e)
        },
        _getTextAreaElementToReplaceWithCodeMirror: function() {
            return $(".code-wrap #" + this.type)[0]
        },
        _bindAutoComplete: function() {
            var e = this._getEditorConfig();
            e.autocomplete === !0 && "html" !== e.syntax && this.editor.on("inputRead", function(e, t) {
                !e.state.completionActive && "+input" === t.origin && 1 === t.text.length && /^[a-zA-Z]+$/.test(t.text[0]) && CodeMirror.commands.autocomplete(e, null, {
                    completeSingle: !1
                })
            })
        },
        _disableUserFromDriving: function() {
            this._canDrive = !1, this.editor.setOption("readOnly", !0)
        },
        _enableUserToDrive: function() {
            this._canDrive = !0, this.editor.setOption("readOnly", !1)
        },
        _preProcessorChanged: function(e) {
            this._turnOffReadOnlyView(), this._changeMode(e)
        },
        _turnOffReadOnlyView: function() {
            this._viewingSource && this.showOriginalCode()
        },
        unbindOnChange: function() {
            this.editor.off("change", this._onEditorChange.bind(this))
        },
        bindToOnChange: function() {
            this.editor.on("change", this._onEditorChange.bind(this))
        },
        _onEditorChange: function(e) {
            if (!this.editor.getOption("readOnly")) {
                var t = {
                    origin: "client",
                    pen: {}
                };
                t.pen[this.type] = e.getValue(), CP.item.setItemValue(t)
            }
        },
        getValue: function() {
            return this.editor.getValue()
        },
        setValue: function(e) {
            this.editor.setValue(e)
        },
        _setEditorValue: function(e) {
            "collab" !== window.__pageType && "professor" !== window.__pageType && (this.unbindOnChange(), this.editor.setValue(e), this.bindToOnChange())
        },
        _setMode: function() {
            var e = this._getBasicType(),
                t = CP.pen[e + "_pre_processor"];
            this._changeMode(t)
        },
        getMode: function() {
            var e = this._getBasicType(),
                t = CP.pen[e + "_pre_processor"];
            return EditorModes.getCMMode(t, e)
        },
        _changeMode: function(e) {
            this.editor.setOption("mode", EditorModes.getCMMode(e, this._getBasicType()))
        },
        _getBasicType: function() {
            throw "Implement in subclass"
        },
        hasFocus: function() {
            return this.editor.hasFocus()
        },
        refresh: function(e, t) {
            this.runRefresh(t.delay)
        },
        runRefresh: function(e) {
            e > 0 ? setTimeout($.proxy(function() {
                this.runRefresh()
            }, this), e) : this._throttledRefresh()
        }
    }),
    BaseEditorViewSourceMixin = {
        showSource: function() {
            this.unbindOnChange(), this._makeEditorReadOnly(), this._changeModeOnShowSource(), this._showSource(), this._removeEventFromEditorUndoHistory()
        },
        _changeModeOnShowSource: function() {
            this._changeMode("none")
        },
        _showSource: function() {
            CP.penErrorHandler.clearPreprocWidgets(this.type), this._setEditorValue(CP.penProcessor.getProcessed(this.type))
        },
        _makeEditorReadOnly: function() {
            $("#box-" + this.type).addClass("view-compiled"), $("#viewsource-" + this.type).attr("title", Copy.returnToSource), this.editor.setOption("readOnly", !0)
        },
        showOriginalCode: function() {
            this._showOriginalCode(), this._changeModeOnShowOriginalCode(), this._makeEditable(), this.bindToOnChange(), this._removeEventFromEditorUndoHistory()
        },
        _changeModeOnShowOriginalCode: function() {
            this._changeMode(CP.pen.getAttribute(this.type + "_pre_processor"))
        },
        _removeEventFromEditorUndoHistory: function() {
            var e = this.editor.getHistory();
            e.done.pop(), e.done.pop(), this.editor.setHistory(e)
        },
        _showOriginalCode: function() {
            this._setEditorValue(CP.pen[this.type])
        },
        _makeEditable: function() {
            $("#box-" + this.type).removeClass("view-compiled view-preproc-errors"), $("#viewsource-" + this.type).attr("title", Copy.viewSource), this._canDrive && this.editor.setOption("readOnly", !1)
        }
    };
! function() {
    function e() {
        window.addEventListener("message", h, !0)
    }

    function t() {
        A = $(".console-entries"), k = $(".console-command-line-input"), M = $(".console-clear-button"), D = k.get(0)
    }

    function n() {
        k.on("keydown", i), k.on("keyup propertychange input", y), M.on("click", C), k.one("focus", b)
    }

    function s() {
        Hub.sub("ui-disable", S), Hub.sub("ui-enable", T), Hub.sub("ui-console-opened", E), Hub.sub("console-opened", P), Hub.sub("console-closed", w), Hub.sub("server-console-change", x), Hub.sub("show-processing-logs", r)
    }

    function i(e) {
        if (y(), e.keyCode && I !== !0) {
            switch (e.keyCode) {
                case 13:
                    if (!e.shiftKey) return e.preventDefault(), void a();
                    break;
                case 38:
                    c(1);
                    break;
                case 40:
                    c(-1)
            }
            "professor" === G && (clearTimeout(K), K = setTimeout(function() {
                o()
            }, q))
        }
    }

    function o(e) {
        var t = {
            console: {
                consoleData: k.val()
            }
        };
        e ? (t.console.command = e, Hub.pub("console-change", t)) : Hub.pub("console-change", t)
    }

    function r(e, t) {
        t.logs.forEach(function(e) {
            f({
                "function": e.type,
                arguments: ["Line " + e.line + " " + e.type.toUpperCase() + ": " + e.message],
                complexity: 1
            })
        })
    }

    function a() {
        var e = k.val();
        if ("" !== e) {
            if ("clear()" === e || "clear();" === e) return void m();
            l(e), k.val(""), g(e), p(e), "professor" === G && (clearTimeout(K), o(e)), y()
        }
    }

    function c(e) {
        1 === e && 0 === H && (U = k.val()), H += e, H < 0 && (H = 0), H > F.length && (H = F.length), 0 === H ? k.val(U) : k.val(F[H - 1]), setTimeout(u, 0)
    }

    function u() {
        if (D.setSelectionRange) {
            var e = 2 * k.val().length;
            D.setSelectionRange(e, e)
        }
    }

    function l(e) {
        H = 0, F.unshift(e), F.length > L && F.pop()
    }

    function d() {
        var e = $(".console-message");
        e.length > L && e.slice(0, e.length - L).remove(), A.scrollTop(A.get(0).scrollHeight)
    }

    function h(e) {
        var t = e.data;
        t.length && "console" === t[0] && (t[1]["function"] && "clear" === t[1]["function"] ? m() : f(t[1]))
    }

    function p(e) {
        R = $(".result-iframe").get(0).contentWindow;
        var t = {
            type: "command",
            command: e
        };
        R.postMessage(t, "*")
    }

    function f(e) {
        if (e.arguments) {
            var t = e.arguments.join(" ");
            t.length > W && (e = J), B === !1 ? (V.push(e), V.length > N && (V.shift(), z = !0)) : _(e)
        }
    }

    function _(e) {
        var t = $(O);
        e["function"] && t.addClass(e["function"]);
        var n = e.arguments,
            s = e.complexity,
            i = n.join(" ");
        if (s > 1) {
            A.append(t);
            var o = CodeMirror(t.get(0), {
                value: i,
                foldGutter: !0,
                readOnly: "nocursor",
                gutters: ["CodeMirror-foldgutter"],
                mode: "javascript"
            });
            if (i.indexOf(": function") !== -1)
                for (var r = o.firstLine(), a = o.lastLine(); r <= a; r++) o.foldCode(CodeMirror.Pos(r, 0), null, "fold")
        } else t.hasClass("error") ? t.text(i) : CodeMirror.runMode(i, {
            name: "javascript",
            json: !0
        }, t.get(0)), A.append(t);
        d()
    }

    function g(e) {
        var t = $(O);
        t.addClass("echo"), t.text(e), A.append(t), d()
    }

    function m() {
        C(), k.val(""), V = [], z = !1, setTimeout(y, 0)
    }

    function v() {
        $(".console-message .CodeMirror").each(function(e, t) {
            t.CodeMirror.refresh()
        }), d()
    }

    function C() {
        $(".console-message").remove()
    }

    function b() {
        var e = D.value;
        D.value = "", j = D.scrollHeight, D.value = e
    }

    function y() {
        if ("" === $(D).val()) D.rows = 1;
        else {
            var e, t = 0 | D.getAttribute("data-min-rows");
            D.rows = t, e = Math.ceil((D.scrollHeight - j) / 15), D.rows = t + e
        }
    }

    function P() {
        B = !0, z === !0 && (_(Y), z = !1);
        for (var e = 0; e < V.length; e++) _(V[e]);
        V = [], setTimeout(v, 1)
    }

    function w() {
        B = !1
    }

    function S() {
        k.addClass("disabled"), I = !0
    }

    function T() {
        k.removeClass("disabled"), I = !1
    }

    function E() {
        k.focus()
    }

    function x(e, t) {
        k.val(t.console.consoleData), t.console.command && (g(t.console.command), p(t.console.command))
    }
    CP.ConsoleEditor = {};
    var A, k, M, D, R, O = "<pre class='console-message CodeMirror-line'></pre>",
        F = [],
        L = 100,
        H = -1,
        U = "",
        I = !1,
        j = 15,
        B = !1,
        V = [],
        N = 11,
        z = !1,
        W = 5e3,
        J = {
            "function": "error",
            arguments: ["Log Skipped: Sorry, this log was too large for our console. You might need to use the browser console instead."],
            complexity: 1
        },
        Y = {
            "function": "error",
            arguments: ["Logs Trimmed: To keep things fast we only stored the last " + (N - 1) + " messages while the CodePen console was closed, the others will be in the browser console."],
            complexity: 1
        },
        G = __pageType,
        K = null,
        q = 250;
    CP.ConsoleEditor.init = function() {
        t(), n(), s(), e()
    }
}();
var CSSEditor = BaseEditor.extend({
    _getBasicType: function() {
        return "css"
    },
    _bindToHub: function() {
        Hub.sub("pen-change", $.proxy(this._onPenChange, this)), Hub.sub("page-loading-done", $.proxy(this._onPageLoadingDone, this))
    },
    _onPenChange: function(e, t) {
        ObjectUtil.hasNestedValue(t, "pen.css_pre_processor") && this._preProcessorChanged(t.pen.css_pre_processor)
    },
    _onPageLoadingDone: function() {
        this.mobile && setTimeout($.proxy(function() {
            this.editor.focus()
        }, this), 400)
    },
    _getEditorTypeSpecificConfig: function() {
        return {
            mode: this.getMode(),
            syntax: "css"
        }
    },
    _setEditorTypeSpecificOptions: function(e) {
        var t = {};
        e.extraKeys && (t = e.extraKeys), t["Ctrl-Space"] = "autocomplete", this.editor.setOption("extraKeys", CodeMirror.normalizeKeyMap(t))
    },
    _emmetSupportedSyntaxes: function() {
        return _.difference(__preprocessors.css.syntaxes, __preprocessors.css.exclude_emmet_syntaxes)
    }
});
! function() {
    function e() {
        for (var e = $(".editor-dropdown"), n = 0; n < e.length; n++) t(e.eq(n))
    }

    function t(e) {
        if (e) {
            var t = e.data("dropdown-type"),
                n = $(".maximize", e);
            n.click(function() {
                Hub.pub("editor-expand", t), Hub.pub("popup-close")
            });
            var s = $(".minimize", e);
            s.click(function() {
                Hub.pub("editor-close", t), Hub.pub("popup-close")
            })
        }
    }
    CP.editorDropDowns = {}, CP.editorDropDowns.init = function() {
        e(), t()
    }
}();
var EditorModes = {
        htmlModes: {
            text: "text",
            none: {
                name: "htmlmixed",
                htmlMode: !0,
                tags: {
                    script: [
                        ["type", /^x-template$/, "htmlmixed"],
                        ["type", /^x-shader\/x-vertex$/, "text/typescript"],
                        ["type", /^x-text\/handlebars$/, "htmlmixed"],
                        ["type", /^text\/paperscript$/, "text/javascript"],
                        [null, null, "text/javascript"]
                    ]
                }
            },
            html: {
                name: "htmlmixed",
                htmlMode: !0,
                tags: {
                    script: [
                        ["type", /^x-template$/, "htmlmixed"],
                        ["type", /^x-shader\/x-vertex$/, "text/typescript"],
                        ["type", /^x-text\/handlebars$/, "htmlmixed"],
                        ["type", /^x-text\/paperscript$/, "text/javascript"],
                        [null, null, "text/javascript"]
                    ]
                }
            },
            haml: "haml",
            slim: "application/x-slim",
            markdown: "markdown",
            pug: "pug"
        },
        htmlLoaderModes: {
            text: "text",
            none: "xml",
            html: "xml",
            haml: "haml",
            slim: "application/x-slim",
            markdown: "markdown",
            pug: "pug"
        },
        cssModes: {
            none: "text/css",
            css: "text/css",
            postcss: "text/css",
            scss: "text/x-scss",
            stylus: "text/x-styl",
            less: "text/x-less",
            sass: "text/x-sass"
        },
        jsModes: {
            none: "text/javascript",
            js: "text/javascript",
            coffeescript: "text/x-coffeescript",
            livescript: "text/x-livescript",
            typescript: "text/typescript",
            babel: "text/jsx"
        },
        getLoaderMode: function(e, t) {
            return "html" === t ? e in this.htmlLoaderModes ? "mode_" + this.htmlLoaderModes[e] : "mode_text" : "css" === t ? e in this.cssModes ? "mode_" + this.cssModes[e] : "mode_text" : "js" === t ? e in this.jsModes ? "mode_" + this.jsModes[e] : "mode_text" : void 0
        },
        getCMMode: function(e, t) {
            return "html" === t ? e in this.htmlModes ? this.htmlModes[e] : "text" : "css" === t ? e in this.cssModes ? this.cssModes[e] : "text" : "js" === t ? e in this.jsModes ? {
                name: this.jsModes[e],
                globalVars: !0
            } : "text" : void 0
        }
    },
    HTMLEditor = BaseEditor.extend({
        _getBasicType: function() {
            return "html"
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.html_pre_processor") && this._preProcessorChanged(t.pen.html_pre_processor)
        },
        _getEditorTypeSpecificConfig: function() {
            var e = !0;
            return __pageType && "embed" === __pageType && (e = !1), {
                mode: this.getMode(),
                syntax: "html",
                profile: "xhtml",
                autofocus: e
            }
        },
        _completeAfter: function(e, t) {
            return t && !t() || setTimeout(function() {
                e.state.completionActive || e.showHint({
                    completeSingle: !1
                })
            }, 100), CodeMirror.Pass
        },
        _completeIfAfterLt: function(e) {
            return this._completeAfter(e, function() {
                var t = e.getCursor();
                return "<" == e.getRange(CodeMirror.Pos(t.line, t.ch - 1), t)
            })
        },
        _completeIfInTag: function(e) {
            return this._completeAfter(e, function() {
                var t = e.getTokenAt(e.getCursor());
                if ("string" == t.type && (!/['"]/.test(t.string.charAt(t.string.length - 1)) || 1 == t.string.length)) return !1;
                var n = CodeMirror.innerMode(e.getMode(), t.state).state;
                return n.tagName
            })
        },
        _setEditorTypeSpecificOptions: function(e) {
            var t = {};
            e.extraKeys && (t = e.extraKeys), t = CodeMirror.normalizeKeyMap(t), e.autocomplete === !0 && (t["Ctrl-Space"] = "autocomplete", t["'<'"] = this._completeAfter.bind(this), t["'/'"] = this._completeIfAfterLt.bind(this), t["' '"] = this._completeIfInTag.bind(this), t["'='"] = this._completeIfInTag.bind(this)), this.editor.setOption("extraKeys", t)
        }
    }),
    JSEditor = BaseEditor.extend({
        _getBasicType: function() {
            return "js"
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.js_pre_processor") && this._preProcessorChanged(t.pen.js_pre_processor)
        },
        _getEditorTypeSpecificConfig: function() {
            return {
                mode: this.getMode()
            }
        },
        _setEditorTypeSpecificOptions: function() {
            this.editor.setOption("extraKeys", CodeMirror.normalizeKeyMap({
                Tab: "respectfulTab",
                "Ctrl-Space": "autocomplete"
            }))
        }
    });
CP.codeEditorResizeController = {
    init: function() {
        this.model = CP.CodeEditorResizeModel, this.events = CP.CodeEditorsResizeEvents, this.view = CP.CodeEditorsResizeView, this.model.init(), this.events.init(this), this.view.init(this.model)
    },
    toggle: function(e) {
        this.model.toggle(e)
    },
    open: function(e) {
        this.model.open(e)
    },
    close: function(e) {
        this.model.close(e)
    },
    expand: function(e) {
        this.model.expand(e)
    },
    resetSizes: function() {
        this.model.resetSizes()
    },
    syncWithServer: function(e) {
        this.model.syncWithServer(e)
    },
    updateEditorSizes: function(e) {
        this.model.updateEditorSizes(e)
    },
    getOpenEditorCount: function() {
        return this.model.getOpenEditorCount()
    },
    getEditorPositions: function() {
        return this.model.getEditorPositions()
    },
    onWindowResize: function() {
        this.view.onWindowResize()
    }
};
var EnableDisableDriver = {
    _canDrive: !0,
    bindToEnableDisableHubEvents: function() {
        Hub.sub("ui-disable", $.proxy(this._disableUserFromDriving, this)), Hub.sub("ui-enable", $.proxy(this._enableUserToDrive, this))
    },
    _disableUserFromDriving: function() {
        this._canDrive = !1, this._disableUIElements()
    },
    _disableUIElements: function() {
        this._disableAllElements(this._getAllUIElements())
    },
    _disableAllElements: function(e) {
        _.forEach(e, function(e) {
            e && e.attr("disabled", !0)
        })
    },
    _enableUserToDrive: function() {
        this._canDrive = !0, this._enableUIElements()
    },
    _enableUIElements: function() {
        this._enableAllElements(this._getAllUIElements())
    },
    _enableAllElements: function(e) {
        _.forEach(e, function(e) {
            e && e.attr("disabled", !1)
        })
    }
};
! function() {
    function e() {
        Hub.sub("server-ui-change", t), Hub.sub("ui-change", n), Hub.sub("editor-sizes-change", o), Hub.sub("editor-expand", i), Hub.sub("editor-close", c), Hub.sub("editor-reset-sizes", r)
    }

    function t(e, t) {
        "editorSizes" in t.ui && d.syncWithServer(t)
    }

    function n(e, t) {
        t.ui && t.ui.editorSizes && s()
    }

    function s() {
        CP.CodeEditorsResizeEvents._canDrive && d.model.TOP_TYPES.forEach(function(e) {
            var t = $("#box-" + e),
                n = CP.ui.editorSizes[e],
                s = 0 === n;
            t.find(".editor-actions-right button").not(".close-editor-button").attr("disabled", s), s = 0 === n || 1 === n, t.find(".close-editor-button").attr("disabled", s);
        })
    }

    function i(e, t) {
        CP.CodeEditorsResizeEvents._canDrive && d.expand(t)
    }

    function o(e, t) {
        d.updateEditorSizes(t)
    }

    function r() {
        CP.CodeEditorsResizeEvents._canDrive && d.resetSizes()
    }

    function a() {
        $(window).on("resize", u)
    }

    function c(e, t) {
        CP.CodeEditorsResizeEvents._canDrive && d.close(t)
    }

    function u() {
        clearTimeout(h), h = setTimeout(l, 100)
    }

    function l() {
        d.onWindowResize()
    }
    CP.CodeEditorsResizeEvents = {
        _canDrive: !0
    };
    var d, h, p = $(".top-boxes .close-editor-button");
    _.extend(CP.CodeEditorsResizeEvents, EnableDisableDriver), CP.CodeEditorsResizeEvents._getAllUIElements = function() {
        return [p]
    }, CP.CodeEditorsResizeEvents.init = function(t) {
        d = t, this.bindToEnableDisableHubEvents(), a(), e(), s()
    }
}(),
    function() {
        function e(e) {
            if (CP.CodeEditorResizeModel.TOP_TYPES.indexOf(e) === -1) throw "Bad editor type: '" + e + "'"
        }

        function t() {
            var e = {
                ui: {
                    editorSizes: CP.ui.editorSizes
                }
            };
            Hub.pub("ui-change", e)
        }

        function n() {
            var e = 0;
            return this.TOP_TYPES.forEach(function(t) {
                var n = CP.ui.editorSizes[t];
                e += 0 !== n ? 1 : 0
            }), e
        }
        CP.CodeEditorResizeModel = {
            TOP_TYPES: ["html", "css", "js"],
            INITIAL_CONSOLE_SIZES: {
                0: "closed",
                1: 1 / 3,
                2: 1
            },
            init: function() {
                var e = _getQueryString("editors");
                if (e && "111" !== e && "1110" !== e) {
                    "000" === e.substring(0, 3) && (e = "111" + e.charAt(3));
                    var t = e.substring(0, 3).split(""),
                        n = 0;
                    t.forEach(function(e) {
                        n += parseInt(e, 10)
                    }), this.TOP_TYPES.forEach(function(t, s) {
                        CP.ui.editorSizes[t] = e[s] / n
                    });
                    var s = e.slice(3, 4),
                        i = this.INITIAL_CONSOLE_SIZES[s] || "closed";
                    CP.ui.editorSizes.console = i
                }
            },
            toggle: function(t) {
                e(t);
                var n = 0 === CP.ui.editorSizes[t];
                this[n ? "open" : "close"](t)
            },
            close: function(n) {
                e(n);
                var s = CP.ui.editorSizes[n];
                if (0 !== s && 1 !== s) {
                    CP.ui.editorSizes[n] = 0;
                    var i = 0;
                    this.TOP_TYPES.forEach(function(e) {
                        var t = CP.ui.editorSizes[e];
                        i += t
                    }), this.TOP_TYPES.forEach(function(e) {
                        CP.ui.editorSizes[e] = CP.ui.editorSizes[e] / i
                    }), t()
                }
            },
            open: function(s) {
                if (e(s), 0 === CP.ui.editorSizes[s]) {
                    var i = n(),
                        o = 1 / (i + 1);
                    this.TOP_TYPES.forEach(function(e) {
                        var t = e === s;
                        CP.ui.editorSizes[e] = t ? o : CP.ui.editorSizes[e] * (1 - o)
                    }), t()
                }
            },
            expand: function(n) {
                e(n), this.TOP_TYPES.forEach(function(e) {
                    CP.ui.editorSizes[e] = e === n ? 1 : 0
                }), t()
            },
            syncWithServer: function(e) {
                this.updateEditorSizes(e.ui.editorSizes)
            },
            updateEditorSizes: function(e) {
                for (var n in e) CP.ui.editorSizes[n] = e[n];
                t(), Hub.pub("save-editor-sizes")
            },
            getEditorPositions: function() {
                var e = [],
                    t = 0;
                return this.TOP_TYPES.forEach(function(n) {
                    e.push(t);
                    var s = CP.ui.editorSizes[n];
                    t += s
                }), e
            },
            resetSizes: function() {
                var e = {},
                    t = this.TOP_TYPES.length;
                this.TOP_TYPES.forEach(function(n) {
                    e[n] = 1 / t
                }), this.updateEditorSizes(e)
            }
        }
    }(), BarDragger.prototype = Object.create(Unidragger.prototype), BarDragger.prototype.staticClick = function(e, t) {
    this.emitEvent("staticClick", [e, t]), this.didFirstClick ? (this.emitEvent("doubleClick", [e, t]), delete this.didFirstClick, clearTimeout(this.doubleClickTimeout)) : (this.didFirstClick = !0, this.doubleClickTimeout = setTimeout(function() {
        delete this.didFirstClick
    }.bind(this), BarDragger.DOUBLE_CLICK_TIME))
}, BarDragger.DOUBLE_CLICK_TIME = 350,
    function(e) {
        "use strict";

        function t(e) {
            if (e) {
                if ("string" == typeof s[e]) return e;
                e = e.charAt(0).toUpperCase() + e.slice(1);
                for (var t, i = 0, o = n.length; i < o; i++)
                    if (t = n[i] + e, "string" == typeof s[t]) return t
            }
        }
        var n = "Webkit Moz ms Ms O".split(" "),
            s = document.documentElement.style;
        "function" == typeof define && define.amd ? define(function() {
            return t
        }) : "object" == typeof exports ? module.exports = t : e.getStyleProperty = t
    }(window),
    function() {
        function e(e, r, a, c) {
            var u = !1;
            for (var l in r)
                if (o.style[l] = r[l], u = e.style[l] == o.style[l], !u) break;
            if (!u) {
                var d = [];
                for (l in r) d.push(l);
                e.style[n] = d.join(""), e.style[s] = a || "0.4s", e.addEventListener(i, function(e) {
                    t(e, c)
                }, !1);
                e.offsetHeight;
                for (l in r) e.style[l] = r[l]
            }
        }

        function t(e, o) {
            e.target.style[n] = null, e.target.style[s] = null, e.target.removeEventListener(i, t, !1), o && o()
        }
        var n = getStyleProperty("transitionProperty"),
            s = getStyleProperty("transitionDuration"),
            i = {
                WebkitTransitionProperty: "webkitTransitionEnd",
                MozTransitionProperty: "transitionend",
                OTransitionProperty: "otransitionend",
                transitionProperty: "transitionend"
            }[n],
            o = document.createElement("div");
        window.triggerTransition = e
    }(),
    function() {
        function e() {
            t(), E.each(n), T.find(".powers-drag-handle").each(n), T.each(function(e, t) {
                var n = $(t).find(".box-title").clone();
                n.addClass("box-title--resizer"), E.eq(e).append(n)
            })
        }

        function t() {
            b = E.width(), y = E.outerHeight(), P = A.outerHeight()
        }

        function n(e, t) {
            var n = new BarDragger(t);
            n.index = e, n.on("doubleClick", r), e > 0 && (n.on("dragStart", s), n.on("dragMove", i), n.on("dragEnd", o))
        }

        function s() {
            d(this)
        }

        function i(e, t, n) {
            p(this, n)
        }

        function o() {
            f(this)
        }

        function r() {
            _(this)
        }

        function a() {
            Hub.sub("ui-change", c)
        }

        function c(e, t) {
            t.ui && (t.ui.editorSizes && u(e, t), t.ui.layout && l(e, t))
        }

        function u(e, t) {
            CP.CodeEditorsResizeView.setEditorSizes(t.ui.editorSizes, !0), v(), Hub.pub("editor-refresh", {
                delay: 10
            })
        }

        function l() {
            T.width("").height(""), w.width("").height(""), t(), CP.CodeEditorsResizeView.setEditorSizes(CP.ui.editorSizes, !1)
        }

        function d(e) {
            if (CP.codeEditorResizeController.events) {
                var t = CP.codeEditorResizeController.events._canDrive;
                t && !R && (M = C.getEditorPositions(), D = M[e.index], R = e, h())
            }
        }

        function h() {
            F = "top" === CP.ui.layout ? w.innerWidth() - b * k : w.innerHeight() - (y + P) * k
        }

        function p(e, t) {
            var n = CP.codeEditorResizeController.events._canDrive;
            if (n && e == R) {
                var s = "top" === CP.ui.layout ? "x" : "y",
                    i = D + t[s] / F;
                i = Math.max(0, Math.min(1, i)), M[e.index] = i, M.forEach(function(t, n) {
                    n < e.index ? M[n] = Math.min(t, i) : n > e.index && (M[n] = Math.max(t, i))
                });
                var o = {};
                M.forEach(function(e, t) {
                    var n = M[t + 1];
                    n = void 0 === n ? 1 : n;
                    var s = n - e,
                        i = C.TOP_TYPES[t];
                    o[i] = s
                }), O = o, CP.CodeEditorsResizeView.setEditorSizes(o, !1)
            }
        }

        function f(e) {
            var t = CP.codeEditorResizeController.events._canDrive;
            t && e == R && (Hub.pub("editor-sizes-change", O), Hub.pub("editor-refresh", {
                delay: 0
            }), R = null, O = null)
        }

        function _(e) {
            var t = C.TOP_TYPES[e.index],
                n = 1 === CP.ui.editorSizes[t];
            n ? Hub.pub("editor-reset-sizes") : Hub.pub("editor-expand", t)
        }

        function g(e, t) {
            var n = w.innerWidth();
            e.editor && w.height(e.editor * (S.height() - x.height()));
            var s = 1 - b * k / n;
            C.TOP_TYPES.forEach(function(i, o) {
                var r = e[i],
                    a = T[o];
                r *= s;
                var c = {
                    width: 100 * r + "%"
                };
                t ? triggerTransition(a, c) : $(a).css(c);
                var u = E[o],
                    l = r * n,
                    d = l < 150;
                $(u)[d ? "addClass" : "removeClass"]("is-horiz-skinny")
            })
        }

        function m(e, t) {
            var n = y + P;
            e.editor && w.width(e.editor * window.innerWidth), C.TOP_TYPES.forEach(function(s, i) {
                var o = e[s],
                    r = T[i],
                    a = "calc((100% - (3 * " + n + "px)) * " + o + " + " + P + "px)",
                    c = {
                        height: a
                    };
                t ? triggerTransition(r, c) : $(r).css(c)
            })
        }

        function v() {
            var e = C.TOP_TYPES.map(function(e) {
                var t = CP.ui.editorSizes[e];
                return t > 0 ? "1" : "0"
            });
            e = e.join("");
            var t = L[CP.ui.editorSizes.console];
            void 0 === t && (t = "1"), e += t;
            var n = window.location,
                s = n.protocol + "//" + n.host + n.pathname,
                i = "1110" === e ? "" : "?editors=" + e,
                o = s + i;
            history.replaceState("", "", o)
        }
        CP.CodeEditorsResizeView = {};
        var C, b, y, P, w = $(".top-boxes"),
            S = $(".page-wrap"),
            T = w.find(".box"),
            E = $(".editor-resizer"),
            x = $("#resizer"),
            A = $(".box .powers"),
            k = T.length;
        CP.CodeEditorsResizeView.init = function(t) {
            C = t, e();
            var n = CP.CodeEditorsSizeStorage.getEditorSizes();
            n && (CP.ui.editorSizes = JSON.parse(n)), CP.CodeEditorsResizeView.setEditorSizes(CP.ui.editorSizes, !1), a()
        };
        var M, D, R, O, F;
        CP.CodeEditorsResizeView.setEditorSizes = function(e, t) {
            "top" === CP.ui.layout ? g(e, t) : m(e, t)
        };
        var L = {
            closed: "0",
            1: "2"
        };
        CP.CodeEditorsResizeView.onWindowResize = function() {
            "top" === CP.ui.layout && CP.CodeEditorsResizeView.setEditorSizes(CP.ui.editorSizes, !1)
        }
    }(),
    function() {
        CP.CodeEditorsSizeStorage = {
            init: function() {
                Hub.sub("save-editor-sizes", this.saveEditorSizes)
            },
            saveEditorSizes: function(e, t) {
                if (__user && __item && (0 != __item.id || t) && 1 !== __user.id && __user.id === __item.user_id) {
                    var n = t || __item.slug_hash;
                    CP.ui.editorSizes.time = (new Date).getTime(), CPLocalStorage.setItem(n + "-editor", JSON.stringify(CP.ui.editorSizes))
                }
            },
            getEditorSizes: function() {
                if (__item) return CPLocalStorage.getItem(__item.slug_hash + "-editor")
            }
        }, CP.CodeEditorsSizeStorage.init()
    }(),
    function() {
        function e(e, r, a) {
            var c = !1;
            for (var u in r)
                if (o.style[u] = r[u], c = e.style[u] == o.style[u], !c) break;
            if (!c) {
                var l = [];
                for (u in r) l.push(u);
                e.style[n] = l.join(""), e.style[s] = a || "0.4s", e.addEventListener(i, t, !1);
                e.offsetHeight;
                for (u in r) e.style[u] = r[u]
            }
        }

        function t(e) {
            e.target.style[n] = null, e.target.style[s] = null, e.target.removeEventListener(i, t, !1)
        }
        var n = getStyleProperty("transitionProperty"),
            s = getStyleProperty("transitionDuration"),
            i = {
                WebkitTransitionProperty: "webkitTransitionEnd",
                MozTransitionProperty: "transitionend",
                OTransitionProperty: "otransitionend",
                transitionProperty: "transitionend"
            }[n],
            o = document.createElement("div");
        window.triggerTransition = e
    }();
var CodeEditorsTidyController = Class.extend({
        init: function() {
            this.model = new CodeEditorTidyModel, this.events = new CodeEditorsTidyEvents(this), this.view = new CodeEditorsTidyView(this.model)
        },
        beautify: function(e) {
            this.model.beautify(e)
        },
        disable: function(e, t) {
            this.view.disable(e, t)
        }
    }),
    CodeEditorsTidyEvents = Class.extend({
        $tidyCodeButtons: $(".tidy-code-button"),
        init: function(e) {
            this.controller = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            this.$tidyCodeButtons._on("click", this._onTidyClick, this)
        },
        _onTidyClick: function(e, t) {
            this.controller.beautify(t.data("type")), Hub.pub("popup-close")
        }
    }),
    CodeEditorTidyModel = Class.extend({
        init: function() {
            this.BEAUTIFY_LAMBDA_URL = "https://gmqxehjo54.execute-api.us-west-2.amazonaws.com/production/beautify"
        },
        beautify: function(e) {
            var t = CP[e + "Editor"].editor,
                n = t.getSelection(),
                s = CP.pen[e],
                i = "" !== n;
            i && (s = n), "js" === e && "typescript" === CP.pen.js_pre_processor && (e = "typescript"), "css" === e && ("scss" !== CP.pen.css_pre_processor && "less" !== CP.pen.css_pre_processor || (e = CP.pen.css_pre_processor)), $.post(this.BEAUTIFY_LAMBDA_URL, {
                type: e,
                content: s,
                tabSize: this._getIndentWidth(),
                indentWith: this._getIndentChar()
            }, $.proxy(function(t) {
                this._beautifyLambdaCallback(e, i, t)
            }, this))
        },
        _beautifyLambdaCallback: function(e, t, n) {
            "typescript" === e && (e = "js"), "less" !== e && "scss" !== e || (e = "css");
            var s = CP[e + "Editor"].editor;
            t ? (s.replaceSelection(n, "around"), "js" === e && s.execCommand("indentAuto")) : s.setValue(n)
        },
        _getIndentWidth: function() {
            return "tabs" === CP.pen.editor_settings.indent_with ? 1 : parseInt(CP.pen.editor_settings.tab_size, 10)
        },
        _getIndentChar: function() {
            return "tabs" === CP.pen.editor_settings.indent_with ? "\t" : " "
        }
    }),
    CodeEditorsTidyView = Class.extend({
        $htmlTidyButton: $("#html-tidy-code-button"),
        $cssTidyButton: $("#css-tidy-code-button"),
        $jsTidyButton: $("#js-tidy-code-button"),
        init: function(e) {
            this.data = e, _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToHub(), this._syncStateOfTidyButtons(CP.pen)
        },
        _bindToHub: function() {
            var e = this;
            Hub.sub("pen-change", function(t, n) {
                e._syncStateOfTidyButtons(n.pen)
            })
        },
        _syncStateOfTidyButtons: function(e) {
            ObjectUtil.hasNestedValue(e, "html_pre_processor") && this._hideShowTidyButton(e.html_pre_processor, ["none"], this.$htmlTidyButton), ObjectUtil.hasNestedValue(e, "css_pre_processor") && this._hideShowTidyButton(e.css_pre_processor, ["none", "scss", "less", "postcss"], this.$cssTidyButton), ObjectUtil.hasNestedValue(e, "js_pre_processor") && this._hideShowTidyButton(e.js_pre_processor, ["none", "babel", "typescript"], this.$jsTidyButton)
        },
        _hideShowTidyButton: function(e, t, n) {
            _.include(t, e) ? n.removeClass("hide") : n.addClass("hide")
        },
        _getAllUIElements: function() {
            return [this.$htmlTidyButton, this.$cssTidyButton, this.$jsTidyButton]
        },
        disable: function(e, t) {
            var n;
            switch (e) {
                case "css":
                    n = this.$cssTidyButton;
                    break;
                case "html":
                    n = this.$htmlTidyButton;
                    break;
                case "js":
                    n = this.$jsTidyButton
            }
            t ? n.addClass("disabled") : n.removeClass("disabled")
        }
    }),
    TransitionsUtil = Class.extend({
        _transitionEl: document.createElement("fakeelement"),
        _transitions: {
            transition: "transitionend",
            OTransition: "oTransitionEnd",
            MozTransition: "transitionend",
            WebkitTransition: "webkitTransitionEnd"
        },
        getBrowserTransitionEventName: function() {
            for (var e in this._transitions)
                if (void 0 !== this._transitionEl.style[e]) return this._transitions[e]
        }
    }),
    CodeEditorsCSSTransitionHandler = Class.extend({
        init: function() {
            this._bindToDOM()
        },
        _bindToDOM: function() {
            var e = _.throttle(function() {
                Hub.pub("editor-refresh", {
                    delay: 10
                })
            }, 300);
            $("#box-html, #box-css #box-js").on(this._getBrowserTransitionEventName(), e)
        },
        _getBrowserTransitionEventName: function() {
            var e = new TransitionsUtil;
            return e.getBrowserTransitionEventName()
        }
    }),
    CodeEditorsUtil = {
        getEditorByType: function(e) {
            return "html" === e ? CP.htmlEditor : "css" === e ? CP.cssEditor : CP.jsEditor
        }
    },
    CodeEditorsViewSourceController = Class.extend({
        init: function() {
            this.model = new CodeEditorsViewSourceModel, this.events = new CodeEditorsViewSourceEvents(this), this.view = new CodeEditorsViewSourceView
        },
        toggleViewSource: function(e) {
            this.model.toggleViewSource(e)
        },
        setViewSourceToFalse: function(e) {
            this.model.setViewSourceToFalse(e)
        },
        syncWithServer: function(e) {
            this.model.syncWithServer(e)
        }
    }),
    CodeEditorsViewSourceEvents = Class.extend({
        _canDrive: !0,
        init: function(e) {
            this.controller = e, this._bindToDOM(), this._bindToHub()
        },
        _bindToDOM: function() {
            $(".view-compiled-button")._on("click", this._toggleViewSource, this)
        },
        _bindToHub: function() {
            Hub.sub("ui-disable", $.proxy(this._disableUserFromDriving, this)), Hub.sub("ui-enable", $.proxy(this._enableUserToDrive, this)), Hub.sub("pen-change", $.proxy(this._onPenChange, this)), Hub.sub("server-ui-change", $.proxy(this._onServerUIChange, this)), Hub.sub("live_change", $.proxy(this._penProcessed, this))
        },
        _disableUserFromDriving: function() {
            this._canDrive = !1
        },
        _enableUserToDrive: function() {
            this._canDrive = !0
        },
        _onServerUIChange: function(e, t) {
            "editorViewSource" in t.ui && this.controller.syncWithServer(t)
        },
        _toggleViewSource: function(e, t) {
            this._canDrive && this.controller.toggleViewSource({
                type: this._getType(t)
            })
        },
        _getType: function(e) {
            return $(e).data("type").toLowerCase()
        },
        _onPenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.html_pre_processor") && this.controller.setViewSourceToFalse({
                type: "html"
            }), ObjectUtil.hasNestedValue(t, "pen.css_pre_processor") && this.controller.setViewSourceToFalse({
                type: "css"
            }), ObjectUtil.hasNestedValue(t, "pen.js_pre_processor") && this.controller.setViewSourceToFalse({
                type: "js"
            })
        },
        _penProcessed: function() {
            for (var e = ["html", "css", "js"], t = 0; t < e.length; t++) {
                var n = e[t];
                CP.ui.editorViewSource[n] && (this.controller.toggleViewSource({
                    type: n
                }), this.controller.toggleViewSource({
                    type: n
                }))
            }
        }
    }),
    CodeEditorsViewSourceModel = Class.extend({
        init: function() {},
        toggleViewSource: function(e) {
            var t = !this._getSource(e.type);
            this._setSource(e.type, t), this._publishChangeEvent(e)
        },
        setViewSourceToFalse: function(e) {
            this._setSource(e.type, !1), this._publishChangeEvent(e)
        },
        _getSource: function(e) {
            return CP.ui.editorViewSource[e]
        },
        _setSource: function(e, t) {
            CP.ui.editorViewSource[e] = t
        },
        syncWithServer: function(e) {
            for (var t in e.ui.editorViewSource) CP.ui.editorViewSource[t] = e.ui.editorViewSource[t], this._publishChangeEvent({
                type: t
            })
        },
        _publishChangeEvent: function(e) {
            var t = {
                ui: {
                    editorViewSource: {}
                }
            };
            t.ui.editorViewSource[e.type] = this._getSource(e.type), Hub.pub("ui-change", t)
        }
    }),
    CodeEditorsViewSourceView = Class.extend({
        init: function() {
            this._bindToHub(), _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents()
        },
        _bindToHub: function() {
            Hub.sub("ui-change", $.proxy(this._onUIChange, this))
        },
        _onUIChange: function(e, t) {
            if (t.ui && t.ui.editorViewSource)
                for (var n in t.ui.editorViewSource) this._toggleViewSource(n, t.ui.editorViewSource[n], t)
        },
        _toggleViewSource: function(e, t) {
            var n = CodeEditorsUtil.getEditorByType(e);
            if (t) {
                n.showSource();
                var s = $(".view-compiled-button[data-type='" + e + "']").closest(".box").data("preprocessor");
                $(".view-compiled-button[data-type='" + e + "']").text("View Uncompiled " + s), $(".view-compiled-button[data-type='" + e + "']").addClass("active"), CP.codeEditorTidyController.disable(e, !0)
            } else $(".view-compiled-button[data-type='" + e + "']").text("View Compiled " + e.toUpperCase()), n.showOriginalCode(), $(".view-compiled-button[data-type='" + e + "']").removeClass("active"), CP.codeEditorTidyController.disable(e, !1)
        },
        _getAllUIElements: function() {
            return [$(".view-compiled-button")]
        }
    }),
    Profiled = Class.extend({
        init: function() {
            _.extend(this, __profiled)
        },
        isUserProfiled: function(e) {
            return !this.is_team && e.username === this.username
        }
    }),
    UI = {
        buildDefaultUIData: function() {
            return {
                info: "closed",
                layout: window.__layoutType || "top",
                editorViewSource: {
                    html: !1,
                    css: !1,
                    js: !1
                },
                editorSizes: {
                    html: 1 / 3,
                    css: 1 / 3,
                    js: 1 / 3,
                    console: "closed"
                },
                settings: {
                    pane: "closed",
                    tab: "html",
                    css: {
                        addons: "closed"
                    }
                }
            }
        }
    };
! function() {
    function e(e, n) {
        $.hideModal(), s(e), i(n), t()
    }

    function t() {
        AJAXUtil.put("/pens/transfer_pen/" + window.__item.id, {}, n)
    }

    function n(e) {
        window.history.replaceState({}, document.title, e.url), location.reload()
    }

    function s(e) {
        $("meta[name='csrf-token']").attr("content", e)
    }

    function i(e) {
        window.__user = e, CP.user.updateUser(e)
    }

    function o(e) {
        return !!e.origin.match(window.__CPDATA.host) && "logged-into-codepen" === e.data.type
    }

    function r(e) {
        return !!e.origin.match(window.__CPDATA.host) && "redirect-to-login" === e.data.type
    }

    function a(e) {
        e.preventDefault(), c(), $.hideModal()
    }

    function c() {
        $.cookie("always_save_as_anon", "true", {
            expires: d,
            path: "/"
        })
    }

    function u() {
        return !(window.__user.id > 1) && ("" !== window.__item.slug_hash && (!(window.__item.user_id > 1) && (!$.cookie("always_save_as_anon") && window.__save_as_anon_pen)))
    }

    function l() {
        return !(window.__user.id < 2) && ("" !== window.__item.slug_hash && (!(window.__item.user_id > 1) && window.__save_as_anon_pen))
    }
    _onMessage(function(t) {
        o(t) && e(t.data.csrf, t.data.user), r(t) && (document.location = "/login")
    });
    var d = 7;
    $("body").on("click", "#save-as-anon", a), u() && $.showModal(_fullURL("/accounts/anon/"), "modal-signup"), l() && t()
}();
var Pen = Class.extend({
    id: "",
    title: "",
    description: "",
    slug_hash: "",
    html: "",
    css: "",
    js: "",
    parent: 0,
    "private": !1,
    template: !1,
    html_pre_processor: "none",
    html_classes: "",
    head: "",
    css_pre_processor: "none",
    css_prefix: "neither",
    css_starter: "neither",
    js_pre_processor: "none",
    js_module: !1,
    tags: [],
    newTags: [],
    editor_settings: {},
    resources: [],
    save_disabled: !1,
    autosavingNow: !1,
    last_updated: "",
    updated_at: "",
    errorCode: "",
    saveAttempts: 0,
    MAX_SAVE_ATTEMPTS: 6,
    SAVE_ATTEMPT_TIMEOUT: 1e4,
    fork: !1,
    lastSavedPen: {},
    redirectingToSavedURL: !1,
    init: function() {
        _.extend(this, AJAXUtil), _.extend(this, PenResourcesData), this._loadStoredData(), this._ensureDataIsValid()
    },
    _loadStoredData: function() {
        _.extend(this, __item, !0), this._ensureResourcesValid(), this.lastSavedPen = this.getSaveableCopyOfPen()
    },
    _ensureDataIsValid: function() {
        var e = new PenValidator;
        e.makePenValid(this), e.makePenValid(this.lastSavedPen)
    },
    setItemValue: function(e) {
        var t = this._cloneObject(e);
        t.item && (t.pen = t.item);
        for (var n in t.pen) this._setValue(n, t.pen[n]);
        Hub.pub("pen-change", this._cloneObject(t))
    },
    _cloneObject: function(e) {
        return JSON.parse(JSON.stringify(e))
    },
    _setValue: function(e, t) {
        if (_.isObject(t) && !_.isArray(t))
            for (var n in t) this[e][n] = t[n];
        else this[e] = t;
        this._updateLastUpdated()
    },
    _updateLastUpdated: function() {
        this.last_updated = (new Date).getTime()
    },
    getLastUpdatedAt: function() {
        return this.last_updated || new Date(this.updated_at).getTime()
    },
    getActiveSlugHash: function() {
        return this["private"] ? this.slug_hash_private : this.slug_hash
    },
    getTags: function() {
        return _.uniq(_.clone(this.tags).concat(_.clone(this.newTags)))
    },
    getAttribute: function(e) {
        return this[e]
    },
    newPen: function() {
        window.location = "/pen"
    },
    save: function() {
        this.save_disabled || (this._canSave() ? this._shouldForkPen() ? this._forkPen() : this._savePenToDB() : Hub.pub("pen-errors", this.errorCode))
    },
    forkPenInCurrentState: function() {
        this.save_disabled || (this._canSave() ? this._forkPen() : Hub.pub("pen-errors", this.errorCode))
    },
    _shouldForkPen: function() {
        return this._isPenOwnedByAnotherUser()
    },
    saveAsPrivate: function() {
        this._canSave() ? (this["private"] = !0, this._savePenToDB()) : Hub.pub("pen-errors", this.errorCode)
    },
    MAX_PEN_SIZE: 1e6,
    _canSave: function() {
        return !CP.user.isUserLoggedIn() && this._isProfessorOrCollabSession() ? (this.errorCode = "anon-cannot-save-during-rt-session", !1) : !(_lengthInUtf8Bytes(JSON.stringify(this.getSaveableCopyOfPen())) > this.MAX_PEN_SIZE) || (this.errorCode = "pen-too-large", !1)
    },
    _isProfessorOrCollabSession: function() {
        return "professor" === __pageType || "collab" === __pageType
    },
    _forkPen: function() {
        this.fork = !0, this._savePenToDB()
    },
    _savePenToDB: function() {
        this._shouldSavePenToDB() ? this.post("/pen/save", {
            pen: this._getSaveablePen()
        }, this.doneSave, this.failedSave, this.erroredSave) : this.sendPseudoSavedSignal()
    },
    _getSaveablePen: function() {
        return JSON.stringify(this.getCopyOfSaveablePen())
    },
    getCopyOfSaveablePen: function() {
        return this._updateSaveableTagsPriorToSave(), this.getSaveableCopyOfPen()
    },
    _shouldSavePenToDB: function() {
        return this.hasPenChanged() || this._isBlankNewPen() || this._isPenOwnedByAnotherUser()
    },
    _isBlankNewPen: function() {
        return "" === this.slug_hash
    },
    _updateSaveableTagsPriorToSave: function() {
        this.tags = this.getTags(), this.newTags = []
    },
    sendPseudoSavedSignal: function() {
        Hub.pub("pen-saved", {})
    },
    doneSave: function(e) {
        this.saveAttempts = 0;
        var t = this._getDiffPen(this, this.lastSavedPen);
        if (this.lastSavedPen = this.getSaveableCopyOfPen(), e.new_pen_saved)
            if (this._isProfessorOrCollabSession()) {
                var n = {
                    newPen: !0,
                    url: e.redirect_url,
                    pen: t
                };
                Hub.pub("pen-saved", n)
            } else Hub.pub("save-editor-sizes", e.slug_hash), CPLocalStorage.setItem(e.slug_hash, "new-pen"), this.redirectingToSavedURL = !0, window.location = this._getFullRedirectURL(e);
        else Hub.pub("pen-saved", {
            pen: t,
            last_saved_time_ago: e.last_saved_time_ago
        })
    },
    _getFullRedirectURL: function(e) {
        var t = document.location.href.split("?");
        return t.length > 1 ? this._stripQuestionMarkInURL(e.redirect_url + "?" + this._stripTemplateQueryPart(t[1])) : e.redirect_url
    },
    _stripTemplateQueryPart: function(e) {
        return (e || "").replace(/template=\w+/, "")
    },
    _stripQuestionMarkInURL: function(e) {
        return e.replace(/\?$/, "")
    },
    failedSave: function(e) {
        this.showStandardErrorMessage(e)
    },
    erroredSave: function() {
        if (this.saveAttempts += 1, this.saveAttempts < this.MAX_SAVE_ATTEMPTS) {
            var e = _.template(Copy.errors["unable-to-save-try-again"], {
                time: TimeUtil.countToString(this.saveAttempts)
            });
            $.showMessage(e, "super-slow"), setTimeout($.proxy(this._savePenToDB, this), this.SAVE_ATTEMPT_TIMEOUT)
        } else this.saveAttempts = 0, $.showMessage(Copy.errors["unable-to-save-ever"], "super-slow")
    },
    isUserPenOwner: function() {
        return "" === this.slug_hash || (CP.profiled.is_team ? CP.profiled.id === CP.user.current_team_id : CP.user.isAnon() ? this.session_hash === CP.user.session_hash : CP.profiled.id === CP.user.id)
    },
    _isPenOwnedByAnotherUser: function() {
        return !this.isUserPenOwner()
    },
    hasPenChanged: function() {
        return _.size(this._getDiffPen(this, this.lastSavedPen)) > 0
    },
    hasPenContentChanged: function() {
        var e = this._getDiffPen(this, this.lastSavedPen);
        return delete e.editor_settings, _.size(e) > 0
    },
    _getDiffPen: function(e, t) {
        return _diffObjects(t, e)
    },
    getSaveableCopyOfPen: function() {
        return _.cloneDeep(_.reduce(this._getDataRelevantAttributes(), function(e, t) {
            return e[t] = this[t], e
        }, {}, this))
    },
    _getDataRelevantAttributes: function() {
        return ["id", "user_id", "team_id", "slug_hash", "slug_hash_private", "title", "description", "tags", "newTags", "resources", "editor_settings", "fork", "private", "parent", "template", "html", "html_pre_processor", "html_classes", "head", "css", "css_pre_processor", "css_prefix", "css_starter", "js", "js_pre_processor", "js_module"]
    },
    getAttributesThatAffectPenPreview: function() {
        return ["html", "css", "js", "html_pre_processor", "html_classes", "head", "css_pre_processor", "css_prefix", "css_starter", "js_pre_processor", "js_module", "resources"]
    }
});
! function() {
    function e() {
        return "penversions:" + CP.user.id + ":" + CP.pen.slug_hash
    }

    function t() {
        var t = localStorage.getItem(e());
        return "string" == typeof t ? JSON.parse(t) : l
    }

    function n(e) {
        var n = t(),
            s = _getUnixTimestamp();
        return n.mostRecentUpdatedAt = s, n.updatedAts.unshift(s), n.updatedAts = n.updatedAts.slice(0, u), n.versions.unshift(e), n.versions = n.versions.slice(0, u), n
    }

    function s() {
        if (!$(".history").hasClass("active")) {
            var t = CP.pen.getSaveableCopyOfPen();
            localStorage.setItem(e(), JSON.stringify(n(t)))
        }
    }

    function i() {
        for (var e = 0, t = localStorage.length; e < t; ++e) {
            var n = localStorage.key(e),
                s = localStorage.getItem(n);
            if (n && n.match(/^penversions:\d+/)) {
                if ("string" != typeof s) continue;
                var i = JSON.parse(s),
                    o = _getUnixTimestamp() - 3 * c;
                i.mostRecentUpdatedAt < o && localStorage.removeItem(n)
            }
        }
    }

    function o() {
        return !1
    }

    function r() {
        o() && (Hub.sub("pen-change", d), setTimeout(i, 1500))
    }
    var a = 6e3,
        c = 86400,
        u = 10,
        l = {
            mostRecentUpdatedAt: 0,
            updatedAts: [],
            versions: []
        },
        d = _.debounce(s, a, {
            leading: !1,
            maxWait: a,
            trailing: !0
        });
    Hub.sub("page-loading-done", r), window.penLocalStorage = {
        getPenVersionsData: t
    }
}();
var PenAutosave = Class.extend({
        autoSaveFunc: null,
        AUTOSAVE_TIMER: 2e4,
        _showedUserAutoSaveOnMessage: !1,
        _hasUserEverSaved: !1,
        _mobile: !1,
        liveChangesWOSave: 0,
        saveButton: $("#save, #update"),
        init: function() {
            this._mobile = window.__mobile, this.autoSaveFunc = _.debounce(this._saveViaAutoSave, this.AUTOSAVE_TIMER, {
                leading: !1,
                trailing: !0
            }), this._bindToHub(), this._startAutoSave()
        },
        _bindToHub: function() {
            Hub.sub("live_change", $.proxy(this._onLiveChange, this)), Hub.sub("pen-saved", $.proxy(this._onSaved, this))
        },
        _startAutoSave: function() {
            this._penCanBeAutosaved() && this._conditionallyShowAutosavingNowMessage()
        },
        _onLiveChange: function() {
            this._penCanBeAutosaved() ? this._handleAutoSave() : this._handleNonAutoSave()
        },
        _handleAutoSave: function() {
            this.autoSaveFunc()
        },
        _saveViaAutoSave: function() {
            this._autoSavePenRightNow() && (CP.pen.autosavingNow = !0, CP.pen.save())
        },
        _autoSavePenRightNow: function() {
            return !$(".history").hasClass("active") && !CP.settingsController.settingsPaneVisible()
        },
        _handleNonAutoSave: function() {
            return !(!CP.pen.isUserPenOwner() || CP.pen.save_disabled) && (this.liveChangesWOSave += 1, this.saveButton.removeClass("unsaved-wobble unsaved-grow"), 6 === this.liveChangesWOSave && this.saveButton.addClass("unsaved-wobble"), 11 === this.liveChangesWOSave && this.saveButton.addClass("unsaved-grow"), 16 === this.liveChangesWOSave && $.showMessage("There have been 15 changes without a save. Remember to save your work!", "slow"), void(21 === this.liveChangesWOSave && this.saveButton.addClass("unsaved-wobble")))
        },
        _onSaved: function() {
            this._hasUserEverSaved = !0, this._showPenSavedMessage(), this._conditionallyShowAutosavingNowMessage()
        },
        _showPenSavedMessage: function() {
            CP.pen.autosavingNow || $.showMessage(Copy.penUpdated), CP.pen.autosavingNow = !1
        },
        _conditionallyShowAutosavingNowMessage: function() {
            this._showedUserAutoSaveOnMessage || this._penCanBeAutosaved() && this._showAutoSaveOnMessage()
        },
        _showAutoSaveOnMessage: function() {
            this._showedUserAutoSaveOnMessage = !0, setTimeout(function() {
                $.showMessage(Copy.autoSavingNow, "slow")
            }, 1500)
        },
        _penCanBeAutosaved: function() {
            return !!this._userInitiatedSavePreviously() && (CP.user.isUserLoggedIn() && CP.pen.editor_settings.auto_save && CP.pen.getActiveSlugHash().length > 0 && !this._mobile)
        },
        _userInitiatedSavePreviously: function() {
            return this._referredFromNewPen() || this._hasUserEverSaved
        },
        _referredFromNewPen: function() {
            return !!document.referrer.match(/\/pen(\/)?$/)
        }
    }),
    PenDelete = Class.extend({
        init: function() {
            _.extend(this, AJAXUtil), this._bindToDOM()
        },
        _bindToDOM: function() {
            $(".delete-button")._on("click", this._deletePen, this)
        },
        _deletePen: function() {
            $.showModal("/ajax/confirm_pen_delete", "modal-warning", $.proxy(this._deletePenModalCallback, this))
        },
        _deletePenModalCallback: function() {
            $("#confirm-delete")._on("click", this._confirmDeletePen, this)
        },
        _confirmDeletePen: function() {
            this.del("/pen/" + CP.pen.slug_hash, {}, this._doneDelete), $.showMessage(Copy.deletingPen, "super-slow")
        },
        _doneDelete: function() {
            CPLocalStorage.setItem("pen_deleted", JSON.stringify({
                penID: CP.pen.id,
                activeSlugHash: CP.pen.getActiveSlugHash()
            })), CP.penSaver.skipWarning = !0, window.location = this._getURLToReturnTo()
        },
        _getURLToReturnTo: function() {
            return CP.profiled.base_url + "/pens/" + this._getProfilePageToReturnTo() + "/"
        },
        _getProfilePageToReturnTo: function() {
            var e = "public",
                t = document.referrer.match(/(?:\/([^\/]+)\/?)$/);
            if (t) {
                var n = t[1];
                _.contains(this._possibleProfilePagesToReturnTo(), n) && (e = n)
            }
            return e
        },
        _possibleProfilePagesToReturnTo: function() {
            return ["popular", "public", "private", "forked", "tags"]
        }
    }),
    PenResourcesData = {
        MIN_RESOURCES: 2,
        setResource: function(e, t) {
            this._setResourceURL(_.find(this.resources, {
                view_id: e
            }), t), this._postResourceChange()
        },
        updateResourcesOrder: function(e, t) {
            this._updateResourcesOrder(e, t), this._postResourceChange()
        },
        addEmptyResource: function(e) {
            this._addNewResource(e, ""), this._postResourceChange()
        },
        quickAddResource: function(e, t) {
            this._quickAddResource(e, t), this._postResourceChange(!0)
        },
        deleteResource: function(e) {
            this._deleteResource(e), this._postResourceChange()
        },
        getResourcesByType: function(e) {
            return _.select(this.resources, {
                resource_type: e
            })
        },
        setPenResources: function(e) {
            this.resources = e.pen.resources, this._postResourceChange()
        },
        _ensureResourcesValid: function() {
            this._ensureMinNumberOfResourcesForEachType(), this._storeResourcesByOrder(), this._ensureResourcesHaveViewIDs(), this._ensureResourcesHaveActions()
        },
        _ensureMinNumberOfResourcesForEachType: function() {
            this._ensureMinNumberOfResourcesForType("css"), this._ensureMinNumberOfResourcesForType("js")
        },
        _ensureMinNumberOfResourcesForType: function(e) {
            _.times(this.MIN_RESOURCES - this.getResourcesByType(e).length, function() {
                this.resources.push({
                    resource_type: e,
                    order: 0,
                    url: ""
                })
            }, this)
        },
        _ensureResourcesHaveViewIDs: function() {
            _.forEach(this.resources, function(e) {
                _.isUndefined(e.view_id) && (e.view_id = IDGenerator.generate())
            })
        },
        _ensureResourcesHaveActions: function() {
            _.forEach(this.resources, function(e) {
                _.isUndefined(e.action) && (e.action = "include_" + e.resource_type + "_url")
            })
        },
        _postResourceChange: function(e) {
            this._updateLastUpdated(), this._ensureResourcesValid(), this._publishResourcePenChange(e)
        },
        _quickAddResource: function(e, t) {
            var n = _.find(this.getResourcesByType(e), {
                url: ""
            });
            n ? this._setResourceURL(n, t) : this._addNewResource(e, t)
        },
        _setResourceURL: function(e, t) {
            e.url = t.replace(/\s+/, "")
        },
        _updateResourcesOrder: function(e, t) {
            for (var n = 0, s = this.resources.length; n < s; n++) this.resources[n].resource_type === e && (this.resources[n].order = t[this.resources[n].view_id]);
            this._storeResourcesByOrder()
        },
        _storeResourcesByOrder: function() {
            this.resources = _.sortBy(this.resources, "order")
        },
        _addNewResource: function(e, t) {
            var n = _.reduce(this.resources, function(t, n) {
                return n.resource_type === e && n.order > t && (t = n.order), n
            }, 0);
            this.resources.push({
                resource_type: e,
                order: n + 1,
                url: t || "",
                view_id: IDGenerator.generate(),
                action: "include_" + e + "_url"
            })
        },
        _deleteResource: function(e) {
            _removeFromArrayByIndex(this.resources, _.findIndex(this.resources, {
                view_id: e
            }))
        },
        _publishResourcePenChange: function(e) {
            Hub.pub("pen-change", {
                origin: "client",
                rebind: e,
                pen: {
                    resources: _.cloneDeep(this.resources)
                }
            })
        }
    },
    PenSaver = Class.extend({
        init: function() {
            this.pageType = __pageType, this._warnAboutLostChanges()
        },
        potentialLostWork: function() {
            return this._shouldWarnAboutLostWork()
        },
        _warnAboutLostChanges: function() {
            window.onbeforeunload = $.proxy(function() {
                if (!this.skipWarning) return this._shouldWarnAboutLostWork() ? Copy.youHaveUnsavedChanges : void 0
            }, this)
        },
        _shouldWarnAboutLostWork: function() {
            var e = CP.pen.hasPenContentChanged();
            return "professor" === this.pageType ? e && this._isProfessorInProfessorRoom() : "collab" === this.pageType ? e && this._isPenOwnerInCollabRoom() : e && !CP.pen.redirectingToSavedURL && !CP.pen.save_disabled
        },
        _isProfessorInProfessorRoom: function() {
            var e = !1;
            return "professor" === this.pageType && "undefined" != typeof CP.professor && (e = !0), e
        },
        _isPenOwnerInCollabRoom: function() {
            return "collab" === this.pageType && CP.pen.isUserPenOwner()
        }
    }),
    LocalDataValidator = {
        makeObjValid: function(e, t) {
            for (var n in e) t[n] && (e[n] = this._getValidAttributeValue(t, n, e[n]))
        },
        _getValidAttributeValue: function(e, t, n) {
            var s = e[t];
            return "array" === s["typeof"] ? this._getValidArrayType(n, s) : _.isArray(s["typeof"]) ? this._getValidArrayAttributeValue(n, s) : this._getValidSimpleAttributeValue(n, s)
        },
        _getValidArrayType: function(e, t) {
            return _.isArray(e) ? e : t["default"]
        },
        _getValidArrayAttributeValue: function(e, t) {
            return _.contains(t["typeof"], e) ? e : t["default"]
        },
        _getValidSimpleAttributeValue: function(e, t) {
            return typeof e === t["typeof"] ? e : t["default"]
        }
    },
    PenValidator = Class.extend({
        dataAttributes: {
            title: {
                "typeof": "string",
                "default": ""
            },
            description: {
                "typeof": "string",
                "default": ""
            },
            slug_hash: {
                "typeof": "string",
                "default": ""
            },
            html: {
                "typeof": "string",
                "default": ""
            },
            css: {
                "typeof": "string",
                "default": ""
            },
            js: {
                "typeof": "string",
                "default": ""
            },
            parent: {
                "typeof": "number",
                "default": 0
            },
            "private": {
                "typeof": "boolean",
                "default": !1
            },
            template: {
                "typeof": "boolean",
                "default": !1
            },
            js_module: {
                "typeof": "boolean",
                "default": !1
            },
            html_pre_processor: {
                "typeof": __preprocessors.html.syntaxes,
                "default": __preprocessors.html["default"]
            },
            html_classes: {
                "typeof": "string",
                "default": ""
            },
            head: {
                "typeof": "string",
                "default": ""
            },
            css_pre_processor: {
                "typeof": __preprocessors.css.syntaxes,
                "default": __preprocessors.css["default"]
            },
            css_prefix: {
                "typeof": __preprocessors.css.prefixes,
                "default": __preprocessors.css.default_prefix
            },
            css_starter: {
                "typeof": __preprocessors.css.bases,
                "default": __preprocessors.css.default_base
            },
            styles: {
                "typeof": "array",
                "default": []
            },
            js_pre_processor: {
                "typeof": __preprocessors.js.syntaxes,
                "default": __preprocessors.js["default"]
            },
            scripts: {
                "typeof": "array",
                "default": []
            }
        },
        makePenValid: function(e) {
            LocalDataValidator.makeObjValid(e, this.dataAttributes)
        }
    }),
    Project = Class.extend({
        id: "",
        title: "",
        description: "",
        slug_hash: "",
        parent: 0,
        "private": !1,
        template: !1,
        tags: [],
        newTags: [],
        init: function() {
            _.extend(this, AJAXUtil), this._loadStoredData(), this._ensureDataIsValid()
        },
        _loadStoredData: function() {
            _.extend(this, __item, !0)
        },
        _ensureDataIsValid: function() {},
        getActiveSlugHash: function() {
            return this["private"] ? this.slug_hash_private : this.slug_hash
        },
        getURLSlugHash: function() {
            return this["private"] ? this.slug_hash_private + "/" + this.slug_hash : this.slug_hash
        },
        getTags: function() {
            return _.uniq(_.clone(this.tags).concat(_.clone(this.newTags)))
        },
        setItemValue: function(e) {
            for (var t in e.item) this._setValue(t, e.item[t])
        },
        _setValue: function(e, t) {
            if (_.isObject(t) && !_.isArray(t))
                for (var n in t) this[e][n] = t[n];
            else this[e] = t
        },
        _updateSaveableTagsPriorToSave: function() {
            this.tags = this.getTags(), this.newTags = []
        },
        saveDetails: function() {
            this._updateSaveableTagsPriorToSave(), this.put("/projects/project/" + this.slug_hash, {
                _isJSON: !0,
                json: {
                    payload: {
                        project: {
                            id: this.id,
                            title: this.title,
                            description: this.description,
                            project_files_attributes: [],
                            "private": this["private"],
                            tags: this.tags
                        }
                    }
                }
            }, this._saveComplete)
        },
        _saveComplete: function() {
            this._updateBrowserURL(), CP.SettingsModel.hideSettingsPane(), location.reload()
        },
        _updateBrowserURL: function() {
            if (window.history.replaceState && CP.item.getActiveSlugHash()) {
                var e = URLBuilder.getItemViewURL("details", CP.profiled, CP.itemType, CP.item, !1);
                window.history.replaceState(e, "", e)
            }
        },
        forkProject: function() {
            this.post("/projects/fork", {
                _isJSON: !0,
                json: {
                    source: this.slug_hash
                }
            }, this._successfulFork)
        },
        _successfulFork: function(e) {
            var t = e.payload.project.url;
            window.location = t
        }
    });
! function() {
    function e(e) {
        firebase.auth().signInWithCustomToken(s.token).then(function() {
            o = firebase.database().ref(), e()
        })["catch"](function(e) {
            console.log(e)
        })
    }

    function t(e) {
        return o.child(i.roomID + "/" + e)
    }

    function n(e, t) {
        try {
            var n = t || "";
            "object" == typeof n ? e.set(JSON.parse(JSON.stringify(n))) : e.set(n)
        } catch (e) {
            window._isOnLocalhost() && console.error("Error setting firebase data!", e)
        }
    }
    var s = window.__firebaseData,
        i = window.__rtData,
        o = null;
    s.connectToFirebase && window.firebase && firebase.initializeApp({
        apiKey: s.apiKey,
        authDomain: s.authDomain,
        databaseURL: s.databaseURL
    }), window.CPFirebase = {
        auth: e,
        getRef: t,
        safelySetRef: n
    }
}();
var PenActions = Class.extend({
        init: function() {
            this._bindToDOM()
        },
        _bindToDOM: function() {
            $("#save, #update, #save-details")._on("click", this._savePen, this), $("#pen-details-form")._on("submit", this._savePen, this), $("#save-as-private")._on("click", this._savePenAsPrivate, this), $("#fork")._on("click", this._fork, this), $("#run")._on("click", this._runPen, this), $("#live-view-popout-button")._on("click", this._liveViewPopout, this)
        },
        _savePen: function() {
            CP.pen.save()
        },
        _savePenAsPrivate: function() {
            CP.pen.saveAsPrivate()
        },
        _fork: function() {
            CP.pen.forkPenInCurrentState()
        },
        _runPen: function() {
            CP.penRenderer.processAndRender(!0)
        },
        _liveViewPopout: function() {
            window.open($("#live-link").attr("href")), CP.CodeEditorsResizeView.setEditorSizes({
                editor: "1"
            })
        }
    }),
    ProjectActions = Class.extend({
        init: function() {
            this._bindToDOM()
        },
        _bindToDOM: function() {
            $("#save-details")._on("click", this._saveDetails, this), $("#fork")._on("click", this._fork, this)
        },
        _saveDetails: function() {
            CP.item.saveDetails()
        },
        _fork: function() {
            CP.item.forkProject()
        }
    });
! function() {
    function e() {
        "vim" === CP.pen.editor_settings.key_bindings && (CodeMirror.commands.save = function() {
            CP.pen.save()
        })
    }

    function t() {
        l.on("click", n), d.on("click", i)
    }

    function n(e) {
        e.preventDefault(), h ? i() : s()
    }

    function s() {
        !h && u.length && (u.show(), CP.showPopupOverlay(), h = !0, Hub.pub("popup-open", p))
    }

    function i() {
        h && u.length && (u.hide(), CP.hidePopupOverlay(), h = !1)
    }

    function o() {
        Hub.sub("key", r), Hub.sub("popup-open", a)
    }

    function r(e, t) {
        "esc" === t.key && i()
    }

    function a(e, t) {
        t !== p && i()
    }

    function c() {
        Keytrap.bind("comctrl+e", function() {
            $("#new-comment").focus()
        }, !0), Keytrap.bind("comctrl+s", function() {
            ("collab" !== window.__pageType && "professor" != window.__pageType || CP.pen.isUserPenOwner()) && CP.pen.save()
        }, !0), Keytrap.bind("comctrl+shift+s", function() {
            CP.pen.saveAsPrivate()
        }, !0), Keytrap.bind("comctrl+p", function() {
            CP.pen.newPen()
        }, !0), Keytrap.bind("comctrl+shift+5", function() {
            CP.penRenderer.processAndRender(!0)
        }, !0), Keytrap.bind("comctrl+shift+0", function() {
            if (CP.pen.slug_hash) {
                var e = document.location.href.replace("/pen/", "/full/");
                window.open(e)
            }
        }, !0), Keytrap.bind("comctrl+shift+9", $.proxy(function() {
            n()
        }, this), !0), Keytrap.bind("comctrl+i", function() {
            CP.settingsController.toggleSettingsPane()
        }, !0), Keytrap.bind("comctrl+shift+.", function() {
            CP.htmlEditor.hasFocus() && CP.htmlEditor.editor.execCommand("closeTag")
        }, !0)
    }
    CP.keyBindings = {}, CP.keyBindings.init = function() {
        e(), t(), o(), c()
    };
    var u = $("#keycommands"),
        l = $(".keyboard-commands-button"),
        d = $("#popup-overlay"),
        h = !1,
        p = "keyCommands"
}();
var ClientSidePenValidations = {
        validatePen: function(e) {
            var t = {};
            return this._validateHTML(e, t), this.validateCSS(e, t), t
        },
        _validateHTML: function(e, t) {
            var n = /^(\s+)?<!doctype/i;
            n.test(e.html) && (t.html = {
                line: 1,
                type: "html",
                message: "You don't need a DOCTYPE on CodePen. Just put here what you would normally put in the <body>.",
                level: "warn",
                pretty_name: "HTML"
            })
        },
        validateCSS: function(e, t) {
            if (_.contains(["scss", "sass"], e.css_pre_processor)) {
                var n = /inline-image/i;
                n.test(e.css) && (t.css = {
                    line: this._findErrorLineNumber(e.css, n),
                    type: "css",
                    message: "The function inline-image can not be used on CodePen since it depends on reading images from disk. We've removed it.",
                    level: "warn",
                    pretty_name: "SCSS"
                });
                var s = /image-width\(/i;
                s.test(e.css) && (t.css = {
                    line: this._findErrorLineNumber(e.css, s),
                    type: "css",
                    message: "The function image-width can not be used on CodePen since it depends on reading images from disk. We've removed it.",
                    level: "warn",
                    pretty_name: "SCSS"
                });
                var i = /image-height\(/i;
                i.test(e.css) && (t.css = {
                    line: this._findErrorLineNumber(e.css, i),
                    type: "css",
                    message: "The function image-height can not be used on CodePen since it depends on reading images from disk. We've removed it.",
                    level: "warn",
                    pretty_name: "SCSS"
                })
            }
            if ("less" === e.css_pre_processor) {
                var o = /data-uri/i;
                o.test(e.css) && (t.css = {
                    line: this._findErrorLineNumber(e.css, o),
                    type: "css",
                    message: "The function data-uri can not be used because it's insecure. We've removed it from your LESS code.",
                    level: "warn",
                    pretty_name: "LESS"
                })
            }
            if (e.css) {
                var r = /<style>/i;
                r.test(e.css) && (t.css = {
                    line: this._findErrorLineNumber(e.css, r),
                    type: "css",
                    message: "You don't need to include a style tag. Just put here what you would normally include in the style tag.",
                    level: "warn",
                    pretty_name: "CSS"
                })
            }
            return t
        },
        _findErrorLineNumber: function(e, t) {
            for (var n = e.split("\n"), s = 0, i = n.length; s < i; s++)
                if (t.test(n[s])) return s + 1;
            return 1
        }
    },
    HTMLTemplating = {
        findHTMLTemplatesInPenHTML: function(e) {
            var t = (e || "").match(/(\[\[\[\S+\]\]\])/gm);
            return t || []
        },
        regexReplaceHTMLBeforeProcessing: function(e, t) {
            return this._regexReplaceHTML(e, "regex_replace_before_processing", t)
        },
        regexReplaceHTMLAfterProcessing: function(e, t) {
            return this._regexReplaceHTML(e, "regex_replace", t)
        },
        _regexReplaceHTML: function(e, t, n) {
            for (var s = 0; s < n.length; s++)
                if (n[s].action === t) {
                    var i = this._getWhiteSpacePrefixToTemplate(n[s].text_to_replace, e),
                        o = n[s].text_to_replace;
                    o = o.replace(/\//g, "\\/"), o = o.replace(/\[/g, "\\["), o = o.replace(/\]/g, "\\]");
                    var r = new RegExp(o, "g");
                    e = e.replace(r, this.getContentToReplaceTemplateText(i, n[s].content))
                }
            return e
        },
        _getWhiteSpacePrefixToTemplate: function(e, t) {
            var n = "(\\s+)?" + e.replace(/\[/g, "\\[").replace(/\//g, "\\/").replace(/\]/g, "\\]"),
                s = new RegExp(n, "mg"),
                i = s.exec(t);
            return i ? i[1] : ""
        },
        getContentToReplaceTemplateText: function(e, t) {
            return e ? (t || "").split(/\r|\n/).join(e) : t
        }
    },
    Instrument = {
        do_instrumenting: function(e, t) {
            var n = "if (window.CP.shouldStopExecution(%d)){break;}",
                s = "\nwindow.CP.exitedLoop(%d);\n",
                i = 1,
                o = [];
            return esprima.parse(e, {
                range: !0,
                tolerant: !1,
                sourceType: t,
                jsx: !0
            }, function(e) {
                switch (e.type) {
                    case "DoWhileStatement":
                    case "ForStatement":
                    case "ForInStatement":
                    case "ForOfStatement":
                    case "WhileStatement":
                        var t = 1 + e.body.range[0],
                            r = e.body.range[1],
                            a = n.replace("%d", i),
                            c = "";
                        "BlockStatement" !== e.body.type && (a = "{" + a, c = "}", --t), o.push({
                            pos: t,
                            str: a
                        }), o.push({
                            pos: r,
                            str: c
                        }), o.push({
                            pos: e.range[1],
                            str: s.replace("%d", i)
                        }), ++i
                }
            }), o.sort(function(e, t) {
                return t.pos - e.pos
            }).forEach(function(t) {
                e = e.slice(0, t.pos) + t.str + e.slice(t.pos)
            }), e
        },
        _loopPrependAST: null,
        _exitedAppendAST: null,
        instrumentCode: function(e, t) {
            try {
                if (this._shouldInstrumentJS(e)) {
                    var n = Instrument.do_instrumenting(e.getProcessed("js"), e.getIsPenModule() ? "module" : "script");
                    return n
                }
                return e.getProcessed("js")
            } catch (n) {
                return t.js = {
                    line: n.lineNumber || 1,
                    column: n.column || 1,
                    index: n.index || 1,
                    type: "js",
                    message: n.description || n.message,
                    level: "error",
                    pretty_name: "JavaScript"
                }, e.getProcessed("js")
            }
        },
        _shouldInstrumentJS: function(e) {
            return !!e.getProcessed("js") && !this._hasJSWeCannotInstrument(e)
        },
        _usingJSPreprocessor: function() {
            return _.contains(__preprocessors.js.preprocessors, CP.pen.js_pre_processor)
        },
        _hasJSWeCannotInstrument: function(e) {
            return new RegExp(this._getInstrumentJSRegex(), "i").exec(e.getProcessed("js"))
        },
        _getInstrumentJSRegex: function() {
            return "(" + window.__preprocessors.js.words_we_cannot_instrument.join("|") + ")"
        },
        _clone: function(e) {
            return JSON.parse(JSON.stringify(e))
        }
    },
    PenErrorHandler = Class.extend({
        editorErrorData: {},
        init: function() {
            this.initEditorErrorData(), this.bindToElements(), this._bindToHub()
        },
        initEditorErrorData: function() {
            _.each(["html", "css", "js"], function(e) {
                this.editorErrorData[e] = {
                    preprocErrors: [],
                    preprocErrorLineNums: []
                }
            }, this)
        },
        bindToElements: function() {
            $(".error-icon").on("click", $.proxy(this.toggleSingleInlineError, this))
        },
        _bindToHub: function() {
            Hub.sub("error-in-code", $.proxy(this._onErrorInCode, this))
        },
        toggleSingleInlineError: function(e) {
            var t = $(e.target).closest("div.box").data("type");
            $("#box-" + t + " .inline-editor-error").removeClass("inline-error-hidden"), t = $(e.target).data("type"), this.jumpToFirstError(t), this.hideErrorBar(t), Hub.pub("editor-refresh", {
                delay: 0
            })
        },
        _onErrorInCode: function(e, t) {
            this.handleErrorsInEditor(t)
        },
        canRenderPen: function(e) {
            return !_.find(e, {
                level: "error"
            })
        },
        previousShowErrorsInEditorTimeoutID: 0,
        handleErrorsInEditor: function(e) {
            this.clearPreviousCallToEditorErrors(), this.clearPreprocWidgets("all"), _.size(e) && this.showErrorsInEditor(e)
        },
        clearPreviousCallToEditorErrors: function() {
            clearTimeout(this.previousShowErrorsInEditorTimeoutID)
        },
        clearPreprocWidgets: function(e) {
            "all" === e ? _.each(["html", "css", "js"], function(e) {
                this.clearPreprocWidgetsOfType(e)
            }, this) : this.clearPreprocWidgetsOfType(e), this.hideErrorBar()
        },
        clearPreprocWidgetsOfType: function(e) {
            var t = CodeEditorsUtil.getEditorByType(e);
            if (t) {
                for (var n = this.editorErrorData[e].preprocErrors, s = 0, i = n.length; s < i; s++) n[s].clear();
                t.editor.eachLine(function(e) {
                    t.editor.removeLineClass(e, "background", "line-highlight")
                }), this.editorErrorData[e].preprocErrors = [], this.editorErrorData[e].preprocErrorLineNums = []
            }
        },
        showErrorsInEditor: function(e) {
            this.previousShowErrorsInEditorTimeoutID = setTimeout($.proxy(function() {
                this.showPreprocessorErrors(e)
            }, this), this.inlineErrorTimeout)
        },
        showPreprocessorErrors: function(e) {
            for (var t in e) {
                var n = e[t];
                if (!_.isUndefined(n.line) && !_.isUndefined(n.message)) {
                    var s = CodeEditorsUtil.getEditorByType(n.type),
                        i = this.addInlineEditorWidget(s.editor, n.line, n.message);
                    this.editorErrorData[n.type].preprocErrors.push(i), this.editorErrorData[n.type].preprocErrorLineNums.push(n.line), this.showErrorBar(n.type)
                }
            }
        },
        addInlineEditorWidget: function(e, t, n) {
            var s = $(this._getInlineErrorWidgetHTML(n))[0],
                i = parseInt(t - 1, 10);
            return e.addLineClass(i, "background", "line-highlight"), e.addLineWidget(i, s, {
                coverGutter: !0,
                noHScroll: !0
            })
        },
        _getInlineErrorWidgetHTML: function(e) {
            var t = "<div class='inline-editor-error inline-error-hidden'><div class='inline-error-message'><%= message %></div></div>";
            return _.template(t, {
                message: this._makeMessageSafe(e)
            })
        },
        _makeMessageSafe: function(e) {
            return _htmlEntities(e).replace(/&lt;br \/&gt;/g, "<br />").replace(/-&gt;/g, "->")
        },
        jumpToFirstError: function(e) {
            var t = this.editorErrorData[e].preprocErrorLineNums,
                n = this.getErrorLineNumberToScrollTo(t[0]);
            CodeEditorsUtil.getEditorByType(e).editor.scrollIntoView(n)
        },
        getErrorLineNumberToScrollTo: function(e) {
            return e - 2 > 0 ? e - 2 : 0
        },
        showErrorBar: function(e) {
            $("#error-bar-" + e).show()
        },
        hideErrorBar: function(e) {
            e ? $("#error-bar-" + e).hide() : $(".error-bar").hide()
        },
        editorHasErrors: function(e) {
            return this.editorErrorData[e].preprocErrors.length
        }
    });
! function(e) {
    "use strict";

    function t(e, t) {
        var n = (65535 & e) + (65535 & t),
            s = (e >> 16) + (t >> 16) + (n >> 16);
        return s << 16 | 65535 & n
    }

    function n(e, t) {
        return e << t | e >>> 32 - t
    }

    function s(e, s, i, o, r, a) {
        return t(n(t(t(s, e), t(o, a)), r), i)
    }

    function i(e, t, n, i, o, r, a) {
        return s(t & n | ~t & i, e, t, o, r, a)
    }

    function o(e, t, n, i, o, r, a) {
        return s(t & i | n & ~i, e, t, o, r, a)
    }

    function r(e, t, n, i, o, r, a) {
        return s(t ^ n ^ i, e, t, o, r, a)
    }

    function a(e, t, n, i, o, r, a) {
        return s(n ^ (t | ~i), e, t, o, r, a)
    }

    function c(e, n) {
        e[n >> 5] |= 128 << n % 32, e[(n + 64 >>> 9 << 4) + 14] = n;
        var s, c, u, l, d, h = 1732584193,
            p = -271733879,
            f = -1732584194,
            _ = 271733878;
        for (s = 0; s < e.length; s += 16) c = h, u = p, l = f, d = _, h = i(h, p, f, _, e[s], 7, -680876936), _ = i(_, h, p, f, e[s + 1], 12, -389564586), f = i(f, _, h, p, e[s + 2], 17, 606105819), p = i(p, f, _, h, e[s + 3], 22, -1044525330), h = i(h, p, f, _, e[s + 4], 7, -176418897), _ = i(_, h, p, f, e[s + 5], 12, 1200080426), f = i(f, _, h, p, e[s + 6], 17, -1473231341), p = i(p, f, _, h, e[s + 7], 22, -45705983), h = i(h, p, f, _, e[s + 8], 7, 1770035416), _ = i(_, h, p, f, e[s + 9], 12, -1958414417), f = i(f, _, h, p, e[s + 10], 17, -42063), p = i(p, f, _, h, e[s + 11], 22, -1990404162), h = i(h, p, f, _, e[s + 12], 7, 1804603682), _ = i(_, h, p, f, e[s + 13], 12, -40341101), f = i(f, _, h, p, e[s + 14], 17, -1502002290), p = i(p, f, _, h, e[s + 15], 22, 1236535329), h = o(h, p, f, _, e[s + 1], 5, -165796510), _ = o(_, h, p, f, e[s + 6], 9, -1069501632), f = o(f, _, h, p, e[s + 11], 14, 643717713), p = o(p, f, _, h, e[s], 20, -373897302), h = o(h, p, f, _, e[s + 5], 5, -701558691), _ = o(_, h, p, f, e[s + 10], 9, 38016083), f = o(f, _, h, p, e[s + 15], 14, -660478335), p = o(p, f, _, h, e[s + 4], 20, -405537848), h = o(h, p, f, _, e[s + 9], 5, 568446438), _ = o(_, h, p, f, e[s + 14], 9, -1019803690), f = o(f, _, h, p, e[s + 3], 14, -187363961), p = o(p, f, _, h, e[s + 8], 20, 1163531501), h = o(h, p, f, _, e[s + 13], 5, -1444681467), _ = o(_, h, p, f, e[s + 2], 9, -51403784), f = o(f, _, h, p, e[s + 7], 14, 1735328473), p = o(p, f, _, h, e[s + 12], 20, -1926607734), h = r(h, p, f, _, e[s + 5], 4, -378558), _ = r(_, h, p, f, e[s + 8], 11, -2022574463), f = r(f, _, h, p, e[s + 11], 16, 1839030562), p = r(p, f, _, h, e[s + 14], 23, -35309556), h = r(h, p, f, _, e[s + 1], 4, -1530992060), _ = r(_, h, p, f, e[s + 4], 11, 1272893353), f = r(f, _, h, p, e[s + 7], 16, -155497632), p = r(p, f, _, h, e[s + 10], 23, -1094730640), h = r(h, p, f, _, e[s + 13], 4, 681279174), _ = r(_, h, p, f, e[s], 11, -358537222), f = r(f, _, h, p, e[s + 3], 16, -722521979), p = r(p, f, _, h, e[s + 6], 23, 76029189), h = r(h, p, f, _, e[s + 9], 4, -640364487), _ = r(_, h, p, f, e[s + 12], 11, -421815835), f = r(f, _, h, p, e[s + 15], 16, 530742520), p = r(p, f, _, h, e[s + 2], 23, -995338651), h = a(h, p, f, _, e[s], 6, -198630844), _ = a(_, h, p, f, e[s + 7], 10, 1126891415), f = a(f, _, h, p, e[s + 14], 15, -1416354905), p = a(p, f, _, h, e[s + 5], 21, -57434055), h = a(h, p, f, _, e[s + 12], 6, 1700485571), _ = a(_, h, p, f, e[s + 3], 10, -1894986606), f = a(f, _, h, p, e[s + 10], 15, -1051523), p = a(p, f, _, h, e[s + 1], 21, -2054922799), h = a(h, p, f, _, e[s + 8], 6, 1873313359), _ = a(_, h, p, f, e[s + 15], 10, -30611744), f = a(f, _, h, p, e[s + 6], 15, -1560198380), p = a(p, f, _, h, e[s + 13], 21, 1309151649), h = a(h, p, f, _, e[s + 4], 6, -145523070), _ = a(_, h, p, f, e[s + 11], 10, -1120210379), f = a(f, _, h, p, e[s + 2], 15, 718787259), p = a(p, f, _, h, e[s + 9], 21, -343485551), h = t(h, c), p = t(p, u), f = t(f, l), _ = t(_, d);
        return [h, p, f, _]
    }

    function u(e) {
        var t, n = "",
            s = 32 * e.length;
        for (t = 0; t < s; t += 8) n += String.fromCharCode(e[t >> 5] >>> t % 32 & 255);
        return n
    }

    function l(e) {
        var t, n = [];
        for (n[(e.length >> 2) - 1] = void 0, t = 0; t < n.length; t += 1) n[t] = 0;
        var s = 8 * e.length;
        for (t = 0; t < s; t += 8) n[t >> 5] |= (255 & e.charCodeAt(t / 8)) << t % 32;
        return n
    }

    function d(e) {
        return u(c(l(e), 8 * e.length))
    }

    function h(e, t) {
        var n, s, i = l(e),
            o = [],
            r = [];
        for (o[15] = r[15] = void 0, i.length > 16 && (i = c(i, 8 * e.length)), n = 0; n < 16; n += 1) o[n] = 909522486 ^ i[n], r[n] = 1549556828 ^ i[n];
        return s = c(o.concat(l(t)), 512 + 8 * t.length), u(c(r.concat(s), 640))
    }

    function p(e) {
        var t, n, s = "0123456789abcdef",
            i = "";
        for (n = 0; n < e.length; n += 1) t = e.charCodeAt(n), i += s.charAt(t >>> 4 & 15) + s.charAt(15 & t);
        return i
    }

    function f(e) {
        return unescape(encodeURIComponent(e))
    }

    function _(e) {
        return d(f(e))
    }

    function g(e) {
        return p(_(e))
    }

    function m(e, t) {
        return h(f(e), f(t))
    }

    function v(e, t) {
        return p(m(e, t))
    }

    function C(e, t, n) {
        return t ? n ? m(t, e) : v(t, e) : n ? _(e) : g(e)
    }
    "function" == typeof define && define.amd ? define(function() {
        return C
    }) : "object" == typeof module && module.exports ? module.exports = C : e.md5 = C
}(this);
var PenProcessor = Class.extend({
    errors: {},
    processedPen: null,
    init: function() {
        _.extend(this, AJAXUtil), this.processedPen = new ProcessedPen({
            fullRefresh: !0
        })
    },
    process: function(e, t) {
        async.waterfall([function(e) {
            e(null, {
                processedPen: new ProcessedPen(t)
            })
        }, $.proxy(this._processResources, this), $.proxy(this._processPen, this), $.proxy(this._handleProcessingErrors, this), $.proxy(this._instrumentCode, this), $.proxy(this._cacheResult, this)], function(t, n) {
            e(n)
        })
    },
    _processResources: function(e, t) {
        CP.penResources.processResourcesAndCallback(CP.pen, function(n) {
            e.processedPen.setProcessedValue("resources", n), t(null, e)
        })
    },
    _processPen: function(e, t) {
        e.processingErrors = {};
        var n = e.processedPen,
            s = n.buildPenToSendToPreprocessors();
        if (this._setProcessedValues(e, s.toStore), 0 === Object.keys(s.toSend).length) n.updateSent(n.processed), t(null, e);
        else {
            var i = this;
            AJAXUtil.jwtPost(this._getPreprocessorsURL(s.toSend, this._getJWTToken()), s.toSend, function(n) {
                i._handleProcessorResult(n, e, s, t)
            })
        }
    },
    _handleProcessorResult: function(e, t, n, s) {
        var i = this;
        if (e.errors && e.errors.auth_token_expired) this._renewJwtToken(function() {
            i._processPen(t, function(e, t) {
                s(e, t)
            })
        });
        else {
            var o = t.processedPen.buildProcessedResult(e.payload),
                r = t.processedPen.buildProcessedErrors(e.payload);
            t.processedPen.updateSent(n.toSend), this._setProcessedValues(t, o), this._publishPreprocessorLogs(t.processedPen.reduceAllProcessorLogs(e.payload)), t.processingErrors = r, s(null, t)
        }
    },
    _publishPreprocessorLogs: function(e) {
        Hub.pub("show-processing-logs", {
            logs: e
        })
    },
    _getPreprocessorsURL: function(e, t) {
        var n = md5(JSON.stringify(e));
        return window.__preprocessors_url + "/process/" + n + "?token=" + t
    },
    _getJWTToken: function() {
        return window.__jwt
    },
    _renewJwtToken: function(e) {
        AJAXUtil.post("/processor_jwts", "", function(t) {
            window.__jwt = t.payload.jwt, e()
        })
    },
    _setProcessedValues: function(e, t) {
        _.forEach(["html", "css", "js"], function(n) {
            "undefined" != typeof t[n] && e.processedPen.setProcessedValue(n, t[n])
        })
    },
    _handleProcessingErrors: function(e, t) {
        e.errors = _.assign(e.processingErrors, ClientSidePenValidations.validatePen(CP.pen)), t(null, e)
    },
    _instrumentCode: function(e, t) {
        e.processedPen.setProcessedValue("js", Instrument.instrumentCode(e.processedPen, e.errors)), t(null, e)
    },
    _cacheResult: function(e, t) {
        this.processedPen = e.processedPen, t(null, e)
    },
    getProcessed: function(e) {
        return this.processedPen.getProcessed(e) ? this.processedPen.getProcessed(e) : "\ud83d\udc3b Bear with us while we compile your code to " + e
    }
});
! function() {
    function e(e, t, n) {
        for (var s = 0; s < e.length; s++)
            if (e[s] && e[s][t] && e[s][t] === n) return s;
        return -1
    }

    function t() {
        n()
    }

    function n() {
        var e = CPLocalStorage.getItem(c);
        e && (r = JSON.parse(e))
    }

    function s(t, n, s) {
        if (("css" === t || "js" === t) && void 0 !== s) {
            var o = e(r[t], "name", s);
            o > -1 ? (r[t].splice(o, 1), r[t].unshift({
                name: s,
                url: n
            })) : r[t].unshift({
                name: s,
                url: n
            }), r[t].length > a && (r[t].length = a), i()
        }
    }

    function i() {
        CPLocalStorage.setItem(c, JSON.stringify(r))
    }

    function o(e) {
        return r[e]
    }
    var r = {
            css: [],
            js: []
        },
        a = 6,
        c = "CP_recent_pen_resources";
    CP.PenRecentResourcesManager = {}, CP.PenRecentResourcesManager.addNewPenResource = s, CP.PenRecentResourcesManager.getRecentPenResources = o, t()
}();
var BunkerBox = {
        makeHeadSafe: function(e, t) {
            return e = e || "", t = this._getSafeSandboxType(t), "public" === t && (e = e.replace(/<script.+/gim, "")), e = this.makeHTMLSafe(e, t)
        },
        makeHTMLSafe: function(e, t) {
            return e = e || "", t = this._getSafeSandboxType(t), e = e.replace(/(<.*?\s)(autofocus=("|')autofocus("|')|autofocus)/g, "$1"), e = e.replace(/iframe.+src=.+(&#)/gim, ""), e = e.replace(/autoPlay=true/gim, "autoPlay=false"), e = e.replace(/http-equiv="refresh"/gim, ""), e = this.makeJSSafe(e, t)
        },
        makeJSSafe: function(e, t) {
            return e = e || "", t = this._getSafeSandboxType(t), e = e.replace(/location\.replace\s*\(\s*\)/gim, "location.removedByCodePen()"), e = e.replace(/location\.reload\s*\(\s*\)/gim, "location.removedByCodePen()"), "public" === t && (e = e.replace("beforeunloadreplacedbycodepen"), e = e.replace(/debugger(\s+)?;/gim, ""), e = e.replace(/alert/gim, ""), e = e.replace(/geolocation/gim, ""), e = e.replace(/audiocontext/gim, ""), e = e.replace(/getusermedia/gim, "")), this._isSandboxSupported() || (e = e.replace(/window(\s+)?\[(\s+)?("|")l/gim, ""), e = e.replace(/self(\s+)?\[(\s+)?("|")loc/gim, ""), e = e.replace(/\.submit\(\)/gim, ""), e = e.replace(/fromCharCode/gim, ""), e = e.replace(/\blocation(\s+)?=/gim, "")), e
        },
        _getSafeSandboxType: function(e) {
            return "undefined" == typeof e ? "public" : "public" === e ? "public" : "personal"
        },
        _sandboxSupported: null,
        _isSandboxSupported: function() {
            return null === this._sandboxSupported && (this._sandboxSupported = this._determineIfSanboxIsSupported()), this._sandboxSupported
        },
        _determineIfSanboxIsSupported: function() {
            try {
                return "sandbox" in document.createElement("iframe")
            } catch (e) {
                return !1
            }
        }
    },
    URLUtil = {
        httpsURL: function(e) {
            return this._buildURL("https:", e)
        },
        protocolessURL: function(e) {
            return this._buildURL("", e)
        },
        _buildURL: function(e, t) {
            return e + "//" + document.location.host + t
        }
    },
    IFramePenToHTML = {
        renderPenAsHTML: function(e) {
            var t = [];
            return t.push(this._getHTMLStart(e)), t.push(this._getHeadAndCSS(e)), t.push(this._getHTML(e)), t.push(this._getJS(e)), t.push(this._getHTMLEnd()), t.join("\n")
        },
        _getHTMLStart: function(e) {
            return "<!DOCTYPE html><html lang='en' class='" + e.html_classes + "'>"
        },
        _getHTMLEnd: function() {
            return "</body></html>"
        },
        _getHeadAndCSS: function(e) {
            var t = "";
            return t += "<head>", t += "<script src='" + window.__path_to_console_runner_js + "'></script>", t += "<script src='" + window.__path_to_live_reloader_js + "'></script>", t += "<meta charset='UTF-8'>", t += '<meta name="robots" content="noindex">', t += '<link rel="shortcut icon" type="image/x-icon" href="' + window.__favicon_shortcut_icon + '" />', t += '<link rel="mask-icon" type="" href="' + window.__favicon_mask_icon + '" color="#111" />', t += '<link rel="canonical" href="' + document.location.href + '" />', t += "\n" + e.head + "\n", t += this._getCSSStarterStyles(e), t += this._getPrefixesStyles(e), t += this._getStylesToAddToHead(e), t += '\n<style class="cp-pen-styles">' + e.css + "</style>", t += "</head><body>"
        },
        _getHTML: function(e) {
            return BunkerBox.makeHTMLSafe(e.html, "personal")
        },
        _getCSSStarterStyles: function(e) {
            return "normalize" === e.css_starter ? this._buildCSSLink(window.__pen_normalize_css_url) : "reset" === e.css_starter ? this._buildCSSLink(window.__pen_reset_css_url) : ""
        },
        _getPrefixesStyles: function(e) {
            return "prefixfree" === e.css_prefix ? this._buildScriptLink(window.__pen_prefix_free_url) : ""
        },
        _buildScriptLink: function(e) {
            return "<script src='" + e + "'></script>"
        },
        _getStylesToAddToHead: function(e) {
            return _.reduce(e.styles, function(e, t) {
                return "include_css_url" === t.action ? e += this._getIncludeCSSURL(t) : "prepend_as_css" === t.action && (e += "\n<style>\n" + t.content + "\n</style>"), e
            }, "", this)
        },
        _getIncludeCSSURL: function(e) {
            return this._buildCSSLink(this._getIncludeCSSURLHref(e))
        },
        _buildCSSLink: function(e) {
            return "<link rel='stylesheet prefetch' href='" + e + "'>"
        },
        _getIncludeCSSURLHref: function(e) {
            return this._urlRefersToPenURL(e.url) ? this._ensureEndsInDotType(e.url, "css") : e.url
        },
        _getJS: function(e) {
            return this._getJSScripts(e) + this._getPenJS(e) + this._getCheckJS()
        },
        _getCheckJS: function() {
            return ""
        },
        _getPenJS: function(e) {
            var t = this._getSafeJS(e.js);
            if (t) {
                var n = e.js_module ? 'type="module"' : "";
                return "\n<script " + n + ">" + t + "\n//# sourceURL=pen.js\n</script>"
            }
            return ""
        },
        _getJSScripts: function(e) {
            var t = "";
            return this._includeStopExecutionScript(e) && (t += "<script src='" + __path_to_stop_execution_on_timeout + "'></script>"), t + this._getPenResourcesAsScripts(e.scripts)
        },
        _includeStopExecutionScript: function(e) {
            return e.js
        },
        _getPenResourcesAsScripts: function(e) {
            return _.reduce(e, function(e, t) {
                return "include_js_url" === t.action ? e += "<script src='" + this._getIncludeJSURLHref(t) + "'></script>" : "prepend_as_js" === t.action && (e += "\n<script>" + t.content + "</script>"), e
            }, "", this)
        },
        _getIncludeJSURLHref: function(e) {
            return this._urlRefersToPenURL(e.url) ? this._ensureEndsInDotType(e.url, "js") : e.url
        },
        _urlRefersToPenURL: function(e) {
            return /\S+\/pen\/(\w{5,})/i.test(e)
        },
        _ensureEndsInDotType: function(e, t) {
            var n = (e || "").split("?"),
                s = new RegExp("." + t, "i");
            return s.exec(n[0]) ? e : (n[0] = this._removeTraingForwardSlash(n[0]) + "." + t, n.join("?"))
        },
        _removeTraingForwardSlash: function(e) {
            return e.replace(/\/$/, "")
        },
        _getSafeJS: function(e) {
            return BunkerBox.makeJSSafe(e, "personal")
        }
    };
! function() {
    function e() {
        function e() {
            return Math.floor(65536 * (1 + Math.random())).toString(16).substring(1)
        }
        return e() + e() + "-" + e() + "-" + e() + "-" + e() + "-" + e() + e() + e()
    }
    window.generateGuid = e
}(),
    function() {
        function e() {
            $("#loading-text").remove()
        }

        function t(e, t) {
            for (var n = e.children("iframe"), s = 0, i = n.length; s < i; s++) n[s].id !== t && $(n[s]).remove();
            $("#" + t).removeClass("iframe-visual-update")
        }

        function n(n, s) {
            t(n, s), e()
        }

        function s(e, t) {
            e.prepend(a(t)), i(e, t)
        }

        function i(e, t) {
            var s = !1,
                i = setTimeout(function() {
                    s || (n(e, t), s = !0)
                }, p);
            $("#" + t).load(function() {
                clearTimeout(i), s || (n(e, t), s = !0)
            })
        }

        function o() {
            return window.__CPDATA.iframe_sandbox
        }

        function r() {
            return window.__CPDATA.iframe_allow
        }

        function a(e) {
            return "<iframe id='" + e + "' src='" + c(e) + "' name='CodePen' allowfullscreen='true' sandbox='" + o() + "' allow='" + r() + "' allowTransparency='true' class='result-iframe iframe-visual-update' ></iframe>"
        }

        function c(e) {
            return _.template(u(), {
                key: e,
                domain_iframe: window.__CPDATA.domain_iframe,
                search: document.location.search,
                hash: document.location.hash
            })
        }

        function u() {
            return "<%= domain_iframe %>/boomerang/<%= key %>/index.html<%= search %><%= hash %>"
        }

        function l() {
            return "iFrameKey-" + generateGuid()
        }

        function d(e, t, n) {
            AJAXUtil.post(_fullURL("/boomerang"), {
                key: e,
                html: t
            }, n)
        }

        function h(e, t) {
            var n = l();
            d(n, t, function() {
                s(e, n)
            })
        }
        var p = 300;
        window.IFrameRender = {
            updateIFramePreview: h
        }
    }(),
    function() {
        function e() {
            window[n()](t(), function(e) {
                try {
                    var t = JSON.parse(e.data),
                        n = s(t.line);
                    Hub.pub("error-in-code", {
                        js: {
                            line: n,
                            type: "js",
                            message: o(n),
                            level: "error",
                            pretty_name: "JavaScript"
                        }
                    })
                } catch (e) {}
            }, !1)
        }

        function t() {
            return "attachEvent" === n() ? "onmessage" : "message"
        }

        function n() {
            return window.addEventListener ? "addEventListener" : "attachEvent"
        }

        function s(e) {
            var t = i(e);
            t = t.replace(/\s+/g, "");
            var n = _.map(CP.pen.js.split(/\r\n?|\n/), function(e) {
                return e.replace(/\s+/g, "")
            });
            return _.indexOf(n, t) + 1
        }

        function i(e) {
            for (var t = g.split(/\r\n?|\n/), n = e; n > -1 && t[n].match(/(break;|shouldStopExecution)/);) n -= 1;
            return t[n]
        }

        function o(e) {
            return "Infinite loop found on line " + e + ". The line number is approximated so look carefully."
        }

        function r(e) {
            if (null === b) return b = _.cloneDeep(e, !0), void h(e);
            if (a(e)) try {
                d(e)
            } catch (t) {
                h(e)
            } else h(e)
        }

        function a(e) {
            if (c(e)) return !1;
            if (e.fullRefresh) return !1;
            var t = l(e, b);
            return b = _.clone(e, !0), t
        }

        function c(e) {
            if (u(e)) return !0;
            var t = (e.css || "").match(/(keyframes|:target|animation|CP_DoFullRefresh)/gi);
            return t
        }

        function u(e) {
            var t = e.html || "",
                n = e.css || "";
            return t.match(/svg/i) && n.match(/background/)
        }

        function l(e, t) {
            if (null === t) return !1;
            var n = _diffObjects(e, t);
            return delete n.css, delete n.fullRefresh, 0 === _.size(n)
        }

        function d(e) {
            for (var t = document.getElementsByClassName("result-iframe"), n = 0; n < t.length; n++) t[n].contentWindow.postMessage(JSON.stringify(e), "*")
        }

        function h(e) {
            clearTimeout(C), C = setTimeout(function() {
                p(e), v = m
            }, v)
        }

        function p(e) {
            g = IFramePenToHTML.renderPenAsHTML(e), IFrameRender.updateIFramePreview(f, g)
        }
        var f = $("#result_div, #result-box"),
            g = "",
            m = 400,
            v = 5,
            C = 0,
            b = null;
        e(), window.PenToPreview = {
            updateLiveIFrame: r
        }
    }();
var PenRenderer = Class.extend({
        DEFAULT_TIMEOUT: 700,
        MAX_TIMEOUT: 2500,
        init: function() {
            this._bindToHub(), this.processAndRender = _.debounce(this._processAndRenderInternal.bind(this), this.DEFAULT_TIMEOUT, {
                leading: !1,
                maxWait: this.MAX_TIMEOUT
            }), this.processAndRender(!0)
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            CP.pen.getAttribute("editor_settings").auto_run && this._penPreviewAttributeChanged(t.pen) && this.processAndRender()
        },
        _penPreviewAttributeChanged: function(e) {
            var t = CP.pen.getAttributesThatAffectPenPreview();
            return _.intersection(t, _.keys(e)).length > 0
        },
        _processAndRenderInternal: function(e) {
            CP.penProcessor.process(this._renderProcessedPen.bind(this), {
                fullRefresh: e || !1
            })
        },
        _renderProcessedPen: function(e) {
            CP.penErrorHandler.canRenderPen(e.errors) && (this.renderablePen = e.processedPen.buildRenderableHash(), Hub.pub("live_change", this.renderablePen), PenToPreview.updateLiveIFrame(this.renderablePen)), CP.penErrorHandler.handleErrorsInEditor(e.errors)
        }
    }),
    PenResources = Class.extend({
        _cachedResources: {},
        init: function() {
            _.extend(this, AJAXUtil)
        },
        processResourcesAndCallback: function(e, t) {
            var n = this._findResourcesNeedingToBeProcess(e);
            this._canProcessResources() && n.length > 0 ? this._postPenResources(e, t) : t(e.resources)
        },
        _findResourcesNeedingToBeProcess: function(e) {
            return HTMLTemplating.findHTMLTemplatesInPenHTML(e.html).concat(this._findResourcesThatNeedToBeProcessed(e.resources))
        },
        _findResourcesThatNeedToBeProcessed: function(e) {
            return _.filter(_.pluck(e, "url"), function(e) {
                return this._isURLResourceNeedingProcessing(e)
            }, this)
        },
        _isURLResourceNeedingProcessing: function(e) {
            return this._urlRefersToPenURL(e) || this._urlRefersToCSSNeedingToBePreprocessed(e) || this._urlRefersToJSNeedingToBePreprocessed(e)
        },
        _urlRefersToPenURL: function(e) {
            return /\S+\/pen\/(\w{5,})/i.test(e)
        },
        _urlRefersToCSSNeedingToBePreprocessed: function(e) {
            return _.contains(__preprocessors.css.preprocessors, this._getExtension(e))
        },
        _urlRefersToJSNeedingToBePreprocessed: function(e) {
            return _.contains(__preprocessors.js.preprocessors, this._getExtension(e))
        },
        _getExtension: function(e) {
            return _.last((e || "").split("."))
        },
        _canProcessResources: function() {
            return navigator.cookieEnabled
        },
        _postPenResources: function(e, t) {
            var n = "/process_resources",
                s = {
                    pen: JSON.stringify(this._getProcessResourcesParams(e))
                };
            this.post(n, s, $.proxy(function(e) {
                var n = JSON.parse(e.resources);
                this._cacheResources(n), t(n)
            }, this))
        },
        _getProcessResourcesParams: function(e) {
            return _.pick(e, ["html", "html_pre_processor", "css", "css_pre_processor", "css_prefix", "js_pre_processor", "resources"])
        },
        _cacheResources: function(e) {
            this._cachedResources = {};
            for (var t = 0, n = e.length; t < n; t++) this._cachedResources[this._getCacheKey(e[t])] = e[t]
        },
        _getCacheKey: function(e) {
            return e.url ? e.url : e.text_to_replace
        },
        _hasAllResourcesNeedingToBeProcessedCached: function(e) {
            var t = _.keys(this._cachedResources),
                n = _.difference(e, t);
            return 0 === n.length
        }
    }),
    ProcessedPen = Class.extend({
        fullRefresh: !1,
        processed: {},
        lastSent: {},
        resources: [],
        types: ["html", "css", "js"],
        init: function(e) {
            _.assign(this, e)
        },
        setValue: function(e, t) {
            this[e] = t
        },
        setProcessedValue: function(e, t) {
            this.processed[e] = t
        },
        getProcessed: function(e) {
            return this.processed[e]
        },
        getIsPenModule: function() {
            return CP.pen.js_module
        },
        updateSent: function(e) {
            for (var t in e) this.lastSent[t] = e[t]
        },
        buildPenToSendToPreprocessors: function() {
            var e = {
                html: this._htmlPreprocessorData("html"),
                css: this._cssPreprocessorData("css"),
                js: this._jsPreprocessorData("js")
            };
            return this._diff(this.lastSent, e)
        },
        _htmlPreprocessorData: function(e) {
            var t = this._baseProcessorData(e);
            return t.contentType = "html", t.syntax = CP.pen.html_pre_processor, t.textInput = this._mergeHTMLWithProcessedResources(CP.pen.html), t
        },
        _cssPreprocessorData: function(e) {
            var t = this._baseProcessorData(e);
            return t.contentType = "css", t.syntax = CP.pen.css_pre_processor, t.textInput = this._mergeCSSWithProcessedResources(CP.pen.css, this.processed.resources), "scss" !== t.syntax && "sass" != t.syntax || (t.version = this._determineSassVersion(t.textInput)), "autoprefixer" === CP.pen.css_prefix && (t.options[CP.pen.css_prefix] = !0), t
        },
        _determineSassVersion: function(e) {
            if (!e || "" === e) return "default";
            for (var t = "default", n = !1, s = ["@import compass", "@import 'compass", '@import "compass'], i = 0; i < s.length; i++) e.toLowerCase().indexOf(s[i]) > -1 && (n = !0);
            return n && (t = "3.4.22"), t
        },
        _jsPreprocessorData: function(e) {
            var t = this._baseProcessorData(e);
            return t.contentType = "js", t.syntax = CP.pen.js_pre_processor, t.textInput = this._mergeJSWithProcessedResources(CP.pen.js, this.processed.resources), "none" !== t.syntax && CP.pen.js_module && (t.options.module = !0), t
        },
        _baseProcessorData: function(e) {
            return {
                id: e,
                version: "default",
                options: {}
            }
        },
        _diff: function(e, t) {
            var n = {},
                s = {};
            for (var i in t) {
                var o = !1,
                    r = e[i],
                    a = t[i];
                "" !== a.textInput ? (r ? r.syntax === a.syntax && r.textInput === a.textInput && JSON.stringify(r.options) === JSON.stringify(a.options) || (s[i] = this.processed[i], o = !0) : o = !0, o && ("none" !== a.syntax || "html" !== a.contentType && 0 !== Object.keys(a.options).length || (s[i] = a.textInput, o = !1), o && (n[i] = a))) : s[i] = a.textInput
            }
            return {
                toSend: n,
                toStore: s
            }
        },
        _mergeHTMLWithProcessedResources: function(e) {
            return e ? HTMLTemplating.regexReplaceHTMLBeforeProcessing(e, this.processed.resources) : e
        },
        _mergeCSSWithProcessedResources: function(e, t) {
            return this._getPrependFromResources("css", t) + e
        },
        _mergeJSWithProcessedResources: function(e, t) {
            return this._getPrependFromResources("js", t) + e
        },
        _getPrependFromResources: function(e, t) {
            return _.reduce(t, function(t, n) {
                return n.action === "prepend_to_" + e ? "js" === e ? t + n.content + ";\n" : t + n.content + "\n" : t
            }, "")
        },
        buildProcessedResult: function(e) {
            return this.types.reduce(function(t, n) {
                return e[n] && "undefined" != typeof e[n].textOutput && (t[n] = e[n].textOutput), t
            }, {})
        },
        buildProcessedErrors: function(e) {
            var t = {};
            return _.forEach(this.types, function(n) {
                var s = e[n];
                if (s && s.errors && s.errors[0]) {
                    var i = s.errors[0];
                    t[n] = {
                        level: "error",
                        line: i.line,
                        message: i.message,
                        syntax: s.syntax,
                        type: s.contentType
                    }
                }
            }), t
        },
        reduceAllProcessorLogs: function(e) {
            var t = [];
            for (var n in e) {
                var s = e[n].logs;
                if (e[n].logs) {
                    for (var i = e[n].contentType, o = 0, r = s.length; o < r; o++) t.push({
                        line: s[o].line,
                        message: s[o].message,
                        type: i
                    });
                    t = t.concat()
                }
            }
            return t
        },
        buildRenderableHash: function() {
            return {
                html: this._getProcessedHTML(),
                html_classes: CP.pen.html_classes,
                head: CP.pen.head,
                css: this.processed.css,
                css_prefix: CP.pen.css_prefix,
                css_starter: CP.pen.css_starter,
                styles: this._getStyles(),
                js: this._getProcessedJS(),
                js_module: CP.pen.js_module,
                js_pre_processor: CP.pen.js_pre_processor,
                scripts: this._getScripts(),
                fullRefresh: this.fullRefresh,
                location_hash: window.location.hash
            }
        },
        _getProcessedHTML: function() {
            return HTMLTemplating.regexReplaceHTMLAfterProcessing(this.processed.html, this.processed.resources)
        },
        _getProcessedJS: function() {
            return this.processed.js
        },
        _getStyles: function() {
            return this._getIncludesAndPrependToResources(this.processed.resources, "css")
        },
        _getScripts: function() {
            return this._getIncludesAndPrependToResources(this.processed.resources, "js")
        },
        _getIncludesAndPrependToResources: function(e, t) {
            return _.sortBy(_.select(this._matchByURLAndMixin(e, t), function(e) {
                return _isValidURL(e.url) && (e.action === "include_" + t + "_url" || e.action === "prepend_as_" + t)
            }), "order")
        },
        _matchByURLAndMixin: function(e, t) {
            return _.map(this._selectRenderableResources(t), function(n) {
                var s = this._findMatchByURLAndResourceType(e, n);
                return s ? _.assign({}, s, n, {
                    action: s.action,
                    content: s.content
                }) : (n.action = "include_" + t + "_url", n)
            }, this)
        },
        _selectRenderableResources: function(e) {
            return _.select(CP.pen.getResourcesByType(e), function(e) {
                return "" !== e.url
            })
        },
        _findMatchByURLAndResourceType: function(e, t) {
            return _.find(e, function(e) {
                return e.url === t.url && e.resource_type === t.resource_type
            })
        }
    }),
    BaseSettingsController = Class.extend({
        setItemValue: function(e) {
            this.item || (this.item = this.pen), this.item.setItemValue(e)
        },
        syncWithServer: function(e) {
            this.model.syncWithServer(e)
        }
    }),
    BaseSettingsEvents = Class.extend({
        type: "",
        _canDrive: !0,
        init: function(e) {
            this.controller = e, _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents()
        },
        _setItemValue: function(e) {
            this.controller.setItemValue(this._buildBasicData(e))
        },
        _setItemValueFromServer: function(e) {
            this.controller.setItemValue(e)
        },
        _onServerPenChange: function(e, t) {
            this.controller.setItemValue(t)
        },
        _buildBasicData: function(e) {
            return {
                origin: "client",
                pen: e
            }
        }
    }),
    BaseSettingsModel = Class.extend({
        init: function() {}
    }),
    BaseSettingsView = Class.extend({
        type: "",
        _canDrive: !0,
        init: function() {}
    }),
    BehaviorController = BaseSettingsController.extend({
        init: function(e) {
            this.pen = e, this.events = new BehaviorEvents(this), this.view = new BehaviorView(e)
        }
    }),
    BehaviorEvents = BaseSettingsEvents.extend({
        $autoSave: $("#auto-save"),
        $autoRun: $("#auto-run"),
        $tabSize: $("#tab-size"),
        $indentWith: $("input[name='indent-with']"),
        init: function(e) {
            this._super(e), _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToDOM(), this._bindToHub()
        },
        _bindToDOM: function() {
            this.$autoSave._on("click", this._setAutoSave, this, !0), this.$autoRun._on("click", this._setAutoRun, this, !0), this.$tabSize._on("change", this._onTabSizeChange, this), this.$indentWith._on("click", this._setIndentWith, this, !0)
        },
        _bindToHub: function() {
            Hub.sub("server-pen-change", $.proxy(this._onServerPenChange, this))
        },
        _onServerPenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.editor_settings") && this._setItemValueFromServer(t)
        },
        _setAutoSave: function(e, t) {
            this._canDrive && this._setItemValue(this._buildSettingsPenData("auto_save", t.is(":checked")))
        },
        _setAutoRun: function(e, t) {
            this._canDrive && this._setItemValue(this._buildSettingsPenData("auto_run", t.is(":checked")))
        },
        _setIndentWith: function() {
            this._canDrive && this._setItemValue(this._buildSettingsPenData("indent_with", $("input[name='indent-with']:checked").val()))
        },
        _onTabSizeChange: function(e, t) {
            this._canDrive && this._setItemValue(this._buildSettingsPenData("tab_size", this._validTabSize(t.val())))
        },
        _validTabSize: function(e) {
            var t = 1 * e;
            return t < 1 || t > 6 ? "1" : e
        },
        _buildSettingsPenData: function(e, t) {
            var n = {
                editor_settings: {}
            };
            return n.editor_settings[e] = t, n
        },
        _getAllUIElements: function() {
            return [this.$autoSave, this.$autoRun, this.$tabSize, this.$indentWith]
        }
    }),
    BehaviorView = Class.extend({
        $body: $("body"),
        $autoSave: $("#auto-save"),
        $autoRun: $("#auto-run"),
        $tabSize: $("#tab-size"),
        $indentWithSpaces: $("#indent-with-spaces"),
        $indentWithTabs: $("#indent-with-tabs"),
        init: function(e) {
            this._bindToHub(), this._handlePenChange(e)
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            this._handlePenChange(t.pen)
        },
        _handlePenChange: function(e) {
            ObjectUtil.hasNestedValue(e, "editor_settings.auto_run") && this._setAutoRun(e.editor_settings.auto_run), ObjectUtil.hasNestedValue(e, "editor_settings.auto_save") && this._setAutoSave(e.editor_settings.auto_save), ObjectUtil.hasNestedValue(e, "editor_settings.indent_with") && this._setIndentWith(e.editor_settings.indent_with), ObjectUtil.hasNestedValue(e, "editor_settings.tab_size") && this._setTabSize(e.editor_settings.tab_size)
        },
        _setAutoRun: function(e) {
            e ? (this.$autoRun.prop("checked", !0), this.$body.removeClass("show-run-button")) : (this.$autoRun.prop("checked", !1), this.$body.addClass("show-run-button"))
        },
        _setAutoSave: function(e) {
            this.$autoSave.prop("checked", e)
        },
        _setIndentWith: function(e) {
            "tabs" === e ? (this.$indentWithTabs.prop("checked", !0), this.$indentWithSpaces.prop("checked", !1)) : (this.$indentWithTabs.prop("checked", !1), this.$indentWithSpaces.prop("checked", !0))
        },
        _setTabSize: function(e) {
            this.$tabSize.val(e)
        }
    }),
    CSSSettingsController = BaseSettingsController.extend({
        init: function(e) {
            this.pen = e, this.events = new CSSSettingsEvents(this), this.model = new CSSSettingsModel, this.view = new CSSSettingsView(e), this.resourcesController = new ResourcesController("css")
        },
        showAddons: function(e) {
            this.model.showAddons(e)
        },
        hideAddons: function(e) {
            this.model.hideAddons(e)
        },
        syncWithServer: function(e) {
            this.model.syncWithServer(e)
        }
    });
window.CSSSettingsEvents = BaseSettingsEvents.extend({
    type: "css",
    $cssPreProcessor: $("#css-preprocessor"),
    $starterCSS: $("input[name='startercss']"),
    $cssPrefix: $("input[name='prefix']"),
    $cssNeedAnAddon: $("#css-need-an-addon-button"),
    init: function(e) {
        this._super(e), _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToDOM(), this._bindToHub()
    },
    _bindToDOM: function() {
        this.$cssPreProcessor._on("change", this._selectPreProcessor, this, !0), this.$starterCSS._on("click", this._selectStartCSS, this, !0), this.$cssPrefix._on("click", this._selectCSSPrefix, this, !0), this.$cssNeedAnAddon._on("click", this._onClickNeedAnAddonButton, this)
    },
    _bindToHub: function() {
        Hub.sub("server-ui-change", $.proxy(this._onServerUIChange, this)), Hub.sub("server-pen-change", $.proxy(this._onServerPenChange, this))
    },
    _onServerUIChange: function(e, t) {
        ObjectUtil.hasNestedValue(t, "ui.settings.css.addons") && this.controller.syncWithServer(t)
    },
    _onServerPenChange: function(e, t) {
        (ObjectUtil.hasNestedValue(t, "pen.css_pre_processor") || ObjectUtil.hasNestedValue(t, "pen.css_prefix") || ObjectUtil.hasNestedValue(t, "pen.css_starter")) && this._setItemValueFromServer(t)
    },
    _selectPreProcessor: function(e, t) {
        this._canDrive && (this._setItemValue({
            css_pre_processor: t.val()
        }), this.controller.hideAddons({
            origin: "client"
        }))
    },
    _selectStartCSS: function(e, t) {
        this._canDrive && this._setItemValue({
            css_starter: t.val()
        })
    },
    _selectCSSPrefix: function(e, t) {
        this._canDrive && this._setItemValue({
            css_prefix: t.val()
        })
    },
    _getAllUIElements: function() {
        return [this.$cssPreProcessor, this.$starterCSS, this.$cssPrefix, this.$cssNeedAnAddon]
    },
    _onClickNeedAnAddonButton: function() {
        this._canDrive && ("open" == CP.ui.settings.css.addons ? this.controller.hideAddons({
            origin: "client"
        }) : this.controller.showAddons({
            origin: "client"
        }))
    }
});
var CSSSettingsModel = BaseSettingsModel.extend({
        type: "css",
        init: function() {
            this._super()
        },
        showAddons: function(e) {
            CP.ui.settings.css.addons = "open", this._pubUIChange(e)
        },
        hideAddons: function(e) {
            CP.ui.settings.css.addons = "closed", this._pubUIChange(e)
        },
        syncWithServer: function(e) {
            CP.ui.settings.css.addons = e.ui.settings.css.addons, this._pubUIChange(e)
        },
        _pubUIChange: function(e) {
            Hub.pub("ui-change", {
                origin: e.origin,
                ui: {
                    settings: {
                        css: {
                            addons: CP.ui.settings.css.addons
                        }
                    }
                }
            })
        }
    }),
    CSSSettingsView = BaseSettingsView.extend({
        $boxCSSEl: $("#box-css"),
        $cssPreprocessor: $("#css-preprocessor"),
        $needAnAddon: $("#need-an-addon"),
        $addOns: $("#add-ons"),
        type: "css",
        init: function(e) {
            this._super(), this._bindToHub(), this._updateUI(e)
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this)), Hub.sub("ui-change", $.proxy(this._onUIChange, this))
        },
        _onPenChange: function(e, t) {
            this._updateUI(t.pen)
        },
        _updateUI: function(e) {
            ObjectUtil.hasNestedValue(e, "css_pre_processor") && this._setPreProcessor(e), ObjectUtil.hasNestedValue(e, "css_starter") && this._selectCSSStarter(e), ObjectUtil.hasNestedValue(e, "css_prefix") && this._selectCSSPrefix(e)
        },
        _onUIChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "ui.settings.css.addons") && this._updateAddons(t.ui.settings.css.addons)
        },
        _setPreProcessor: function(e) {
            this._selectPreProcessor(e), this._addClassesToBoxCSS(e), this._toggleNeedAddons(e)
        },
        _selectPreProcessor: function(e) {
            this.$cssPreprocessor.val(e.css_pre_processor)
        },
        _addClassesToBoxCSS: function(e) {
            this.$boxCSSEl.data("preprocessor", __preprocessors.css.pretty_syntaxes[e.css_pre_processor]), this.$boxCSSEl.removeClass(__preprocessors.css.syntaxes.join(" ")).removeClass(__preprocessors.css.prefixes.join(" ")).addClass(e.css_pre_processor)
        },
        _toggleNeedAddons: function(e) {
            if (_.contains(__preprocessors.css.preprocessors, e.css_pre_processor)) {
                var t = this;
                CacheGet.find({
                    key: "/editor/constants/addons/" + e.css_pre_processor,
                    url: "/editor/constants/addons/" + e.css_pre_processor,
                    dataType: "json",
                    onSuccess: function(e) {
                        t.$addOns.html(e.html), t.$needAnAddon.removeClass("hide"), t._bindToAddOnsDOM()
                    }
                })
            } else this.$needAnAddon.addClass("hide")
        },
        _bindToAddOnsDOM: function() {
            this.$addOnFilter = $("#css-add-ons-filter"), this.$addOnFilter._on("keyup", this._filterAddOns, this), this.$addOnFilterClear = $("#css-clear-addon-filter"), this.$addOnFilterClear._on("click", this._clearAddOnsFilter, this), this.$addOnItems = $("#css-add-ons-list").find("li"), this.$addOnItems.find(".add-add-on")._on("click", this._insertAddOnCode, this)
        },
        _insertAddOnCode: function(e, t) {
            e.preventDefault();
            var n = CP.cssEditor.editor.getValue(),
                s = t.parent().find(".add-on-code").html() + "\n" + n;
            CP.cssEditor.editor.setValue(s), $.showMessage("Preprocessor Add-On Added!")
        },
        _updateAddons: function(e) {
            "open" === e ? this.$addOns.removeClass("hide") : this.$addOns.addClass("hide")
        },
        _selectCSSStarter: function(e) {
            $("#startercss-options-form input[value='" + e.css_starter + "']").prop("checked", !0)
        },
        _selectCSSPrefix: function(e) {
            $("#prefix-options-form input[value='" + e.css_prefix + "']").prop("checked", !0), this._addCSSPrefixClassToBoxCSS(e)
        },
        _addCSSPrefixClassToBoxCSS: function(e) {
            this.$boxCSSEl.removeClass(__preprocessors.css.prefixes.join(" ")).addClass(e.css_prefix)
        },
        _filterAddOns: function() {
            var e = this.$addOnFilter.val();
            if ("" !== e) {
                this.$addOnItems.hide(), this.$addOnFilterClear.show();
                for (var t = 0; t < this.$addOnItems.length; t++)
                    if (this.$addOnItems[t].innerHTML.indexOf(e) > -1 && ($(this.$addOnItems[t]).show(), $(this.$addOnItems[t]).hasClass("depends-on")))
                        for (var n = t; n >= 0; n--)
                            if (!$(this.$addOnItems[n]).hasClass("depends-on")) {
                                $(this.$addOnItems[n]).show();
                                break
                            }
            } else this.$addOnFilterClear.hide(), this.$addOnItems.show()
        },
        _clearAddOnsFilter: function() {
            this.$addOnFilter.val(""), this.$addOnItems.show(), this.$addOnFilterClear.hide()
        }
    });
! function() {
    CP.headerController = {}, CP.headerController.init = function() {
        CP.headerEvents.init(), CP.headerView.init()
    }
}(),
    function() {
        function e() {
            r.on("click", n)
        }

        function t() {
            o = $(".inline-title-input"), o.on("keydown", s), o.on("blur", i)
        }

        function n() {
            CP.headerView.enterEditMode(), setTimeout(function() {
                t();
                var e = o.val();
                o.val(""), o.focus(), o.val(e)
            }, 0)
        }

        function s(e) {
            if (e.keyCode) switch (e.keyCode) {
                case 13:
                    o.blur();
                    break;
                case 27:
                    CP.headerView.cancelEditMode()
            }
        }

        function i() {
            CP.headerView.savePenTitle()
        }
        var o, r = ($("#pen-title"), $(".edit-pen-title"));
        CP.headerEvents = {}, CP.headerEvents.init = function() {
            e()
        }
    }(),
    function() {
        function e() {
            n = $("<input class='inline-title-input' type='text' maxlength='255' />"), n.val(CP.pen.title), s.append(n), n.focus(), n[0].setSelectionRange(CP.pen.title.length, CP.pen.title.length)
        }

        function t(e) {
            CP.item.setItemValue({
                origin: "client",
                pen: {
                    title: e
                }
            })
        }
        var n, s = $("#pen-title"),
            i = $(".item-details-title");
        CP.headerView = {}, CP.headerView.init = function() {}, CP.headerView.enterEditMode = function() {
            s.closest(".pen-title-area").addClass("editing"), e()
        }, CP.headerView.cancelEditMode = function() {
            n.remove(), n = null, s.closest(".pen-title-area").removeClass("editing")
        }, CP.headerView.savePenTitle = function() {
            var e = n.val();
            CP.headerView.cancelEditMode(), t(e), "" !== CP.pen.slug_hash && setTimeout(function() {
                CP.pen.save(), i.length && i.val(e)
            }, 1)
        }
    }();
var HTMLSettingsController = BaseSettingsController.extend({
        init: function(e) {
            this.pen = e, this.events = new HTMLSettingsEvents(this), this.model = new HTMLSettingsModel(this), this.view = new HTMLSettingsView(e)
        }
    }),
    HTMLSettingsEvents = BaseSettingsEvents.extend({
        type: "html",
        $htmlPreProcessor: $("#html-preprocessor"),
        $headContent: $("#head-content"),
        $htmlClasses: $("#html-classes"),
        $metaTagInsert: $("#meta-tag-insert"),
        init: function(e) {
            this._super(e), _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToDOM(), this._bindToHub()
        },
        _bindToDOM: function() {
            this.$htmlPreProcessor._on("change", this._selectPreProcessor, this, !0), this.$headContent._on("keyup change", this._setHead, this, !0), this.$htmlClasses._on("keyup change", this._setHTMLClasses, this, !0), this.$metaTagInsert._on("click", this._addCommonMetaTag, this, !1)
        },
        _bindToHub: function() {
            Hub.sub("server-pen-change", $.proxy(this._onServerPenChange, this))
        },
        _onServerPenChange: function(e, t) {
            (ObjectUtil.hasNestedValue(t, "pen.html_pre_processor") || ObjectUtil.hasNestedValue(t, "pen.head") || ObjectUtil.hasNestedValue(t, "pen.html_classes")) && this._setItemValueFromServer(t)
        },
        _selectPreProcessor: function(e, t) {
            this._canDrive && this._setItemValue({
                html_pre_processor: t.val()
            })
        },
        _setHead: function(e, t) {
            this._canDrive && this._setItemValue({
                head: t.val()
            })
        },
        _setHTMLClasses: function(e, t) {
            this._canDrive && this._setItemValue({
                html_classes: t.val()
            })
        },
        _addCommonMetaTag: function() {
            this._canDrive && this._setItemValue({
                head: this._getHeadWithMetaTag()
            })
        },
        _getHeadWithMetaTag: function() {
            return $.trim($("#head-content").val() + '\n<meta name="viewport" content="width=device-width, initial-scale=1">')
        },
        _getAllUIElements: function() {
            return [this.$htmlPreProcessor, this.$headContent, this.$htmlClasses, this.$metaTagInsert]
        }
    }),
    HTMLSettingsModel = BaseSettingsModel.extend({
        type: "html",
        init: function() {
            this._super()
        }
    }),
    HTMLSettingsView = BaseSettingsView.extend({
        $boxHTML: $("#box-html"),
        $htmlPreprocessor: $("#html-preprocessor"),
        $htmlClasses: $("#html-classes"),
        $headWrapper: $(".head-content-wrapper"),
        $head: $("#head-content"),
        type: "html",
        init: function(e) {
            this._super(), this._bindToHub(), this._updateUI(e)
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            this._updateUI(t.pen)
        },
        _updateUI: function(e) {
            ObjectUtil.hasNestedValue(e, "html_pre_processor") && this._setPreProcessor(e.html_pre_processor), ObjectUtil.hasNestedValue(e, "head") && this._setHead(e.head), ObjectUtil.hasNestedValue(e, "html_classes") && this._setHTMLClasses(e.html_classes)
        },
        _setHead: function(e) {
            this.$head.val() !== e && this.$head.val(e), e.indexOf("http://") !== -1 ? this.$headWrapper.addClass("insecure-resource") : this.$headWrapper.removeClass("insecure-resource")
        },
        _setHTMLClasses: function(e) {
            this.$htmlClasses.val() !== e && this.$htmlClasses.val(e)
        },
        _setPreProcessor: function(e) {
            this._addClassBoxHTML(e), this._selectPreProcessor(e)
        },
        _selectPreProcessor: function(e) {
            this.$htmlPreprocessor.val(e)
        },
        _addClassBoxHTML: function(e) {
            this.$boxHTML.data("preprocessor", __preprocessors.html.pretty_syntaxes[e]), this.$boxHTML.removeClass(__preprocessors.html.syntaxes.join(" ")).addClass(e)
        }
    }),
    InfoController = Class.extend({
        init: function() {
            this.events = new InfoEvents, this.view = new InfoView
        }
    }),
    InfoEvents = Class.extend({
        $title: $("#item-details-title"),
        $description: $("#item-details-description"),
        $privateCkbx: $("#item-details-private"),
        $passwordCkbx: $("#item-details-password"),
        $templateCkbx: $("#item-details-template"),
        init: function(e) {
            this.controller = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            this.$title.on("keyup", $.proxy(this._setTitle, this)), this.$description.on("keyup", $.proxy(this._setDescription, this)), this.$privateCkbx._on("click", this._setPrivacy, this, !0), this.$passwordCkbx._on("click", this._setPasswordUsage, this, !0), this.$templateCkbx._on("click", this._setTemplate, this, !0)
        },
        _setTitle: function() {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    title: this.$title.val()
                }
            })
        },
        _setDescription: function() {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    description: this.$description.val()
                }
            })
        },
        _setPrivacy: function() {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    "private": this.$privateCkbx.is(":checked")
                }
            })
        },
        _setPasswordUsage: function() {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    password_used: this.$passwordCkbx.is(":checked")
                }
            })
        },
        _setTemplate: function() {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    template: this.$templateCkbx.is(":checked")
                }
            })
        }
    }),
    InfoView = Class.extend({
        $body: $("body"),
        $lastSavedTimeAgo: $("#last-saved-time-ago"),
        $detailsTitle: $("#details-title"),
        $penTitle: $("#pen-title"),
        $templateCkbx: $("#item-details-template"),
        $passwordInput: $("#item-details-password-value"),
        $passwordArea: $(".password-protect-toggle-wrap"),
        init: function() {
            this.pageType = __pageType, this._bindToHub(), this._hidePenHasUnsavedChanges()
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this)), Hub.sub("pen-errors", $.proxy(this._onPenErrors, this)), Hub.sub("pen-saved", $.proxy(this._onPenSaved, this))
        },
        _onPenChange: function(e, t) {
            this._showPenHasUnsavedChanges(), ObjectUtil.hasNestedValue(t, "pen.title") && this._updatePenTitle(t), ObjectUtil.hasNestedValue(t, "pen.private") && (this._updateBrowserURL(), this._togglePasswordArea(t)), ObjectUtil.hasNestedValue(t, "pen.password_used") && this._togglePasswordInput(t), ObjectUtil.hasNestedValue(t, "pen.template") && this._updateTemplate(t)
        },
        _updatePenTitle: function(e) {
            0 === e.pen.title.length && (e.pen.title = "Untitled Pen"), document.title = e.pen.title;
            var t = $(".pen-title-link", this.$penTitle);
            t.length ? t.text(e.pen.title) : this.$penTitle.text(e.pen.title), this.$detailsTitle.text(e.pen.title)
        },
        _updateBrowserURL: function() {
            if (window.history.replaceState && CP.pen.getActiveSlugHash()) {
                var e = URLBuilder.getViewURL(this.pageType, CP.profiled, CP.pen, !1);
                window.history.replaceState(e, "", e)
            }
        },
        _togglePasswordArea: function(e) {
            e.pen["private"] ? this.$passwordArea.removeClass("hide") : this.$passwordArea.addClass("hide")
        },
        _togglePasswordInput: function(e) {
            e.pen.password_used ? this.$passwordInput.removeClass("hide") : this.$passwordInput.addClass("hide")
        },
        _updateTemplate: function(e) {
            this.$templateCkbx.is(":checked") !== e.pen.template && this.$templateCkbx.attr("checked", e.pen.template)
        },
        _onPenErrors: function(e, t) {
            $.showMessage(Copy.errors[t], "slow")
        },
        _doneWithSignupOrLogin: function(e) {
            CP.user.updateUser(e.user), CP.pen.save()
        },
        _onPenSaved: function(e, t) {
            this._isNewPenInProfessorOrCollab(t) ? $.showMessage(_.template(Copy.penForked, t), 8e3) : (this._hidePenHasUnsavedChanges(), this._updateSavedTimeAgoFooter(t), this._updateTitleAndDescriptionOnDetailsPage())
        },
        _isNewPenInProfessorOrCollab: function(e) {
            return _.contains(["professor", "collab"], this.pageType) && e.newPen
        },
        _updateSavedTimeAgoFooter: function(e) {
            this.$lastSavedTimeAgo.html(e.last_saved_time_ago)
        },
        _updateTitleAndDescriptionOnDetailsPage: function() {
            "details" === this.pageType && window.location.reload()
        },
        _showPenHasUnsavedChanges: function() {
            CP.pen.save_disabled || this.$body.addClass("unsaved")
        },
        _hidePenHasUnsavedChanges: function() {
            this.$body.removeClass("unsaved")
        }
    }),
    JSSettingsController = BaseSettingsController.extend({
        type: "js",
        init: function(e) {
            this.pen = e, this.events = new JSSettingsEvents(this), this.model = new JSSettingsModel(this), this.view = new JSSettingsView(e), this.resourcesController = new ResourcesController("js")
        }
    }),
    JSSettingsEvents = BaseSettingsEvents.extend({
        type: "js",
        $jsPreProcessor: $("#js-preprocessor"),
        $jsModule: $("#js-module"),
        init: function(e) {
            this._super(e), _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToDOM(), this._bindToHub()
        },
        _bindToDOM: function() {
            this.$jsPreProcessor._on("change", this._selectPreProcessor, this, !0), this.$jsModule._on("change", this._selectJSModule, this, !0)
        },
        _bindToHub: function() {
            Hub.sub("server-pen-change", $.proxy(this._onServerPenChange, this))
        },
        _onServerPenChange: function(e, t) {
            (ObjectUtil.hasNestedValue(t, "item.js_pre_processor") || ObjectUtil.hasNestedValue(t, "item.js_module")) && this._setItemValueFromServer(t)
        },
        _selectPreProcessor: function(e, t) {
            this._canDrive && this._setItemValue({
                js_pre_processor: t.val()
            })
        },
        _selectJSModule: function(e, t) {
            this._canDrive && this._setItemValue({
                js_module: t.prop("checked")
            })
        },
        _getAllUIElements: function() {
            return [this.$jsPreProcessor]
        }
    }),
    JSSettingsModel = BaseSettingsModel.extend({
        type: "js",
        init: function() {
            this._super()
        }
    }),
    JSSettingsView = BaseSettingsView.extend({
        $boxJS: $("#box-js"),
        $jsPreprocessor: $("#js-preprocessor"),
        $jsModule: $("#js-module"),
        type: "js",
        init: function(e) {
            this._super(), this._bindToHub(), this._updateUI(e)
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _onPenChange: function(e, t) {
            this._updateUI(t.pen)
        },
        _updateUI: function(e) {
            ObjectUtil.hasNestedValue(e, "js_pre_processor") && this._setPreProcessor(e.js_pre_processor), ObjectUtil.hasNestedValue(e, "js_module") && this._setJSModule(e.js_module)
        },
        _setPreProcessor: function(e) {
            this._addProcessorClassBoxJS(e), this._selectPreProcessor(e)
        },
        _setJSModule: function(e) {
            this.$jsModule.prop("checked", e), this._addModuleClassBoxJS(e)
        },
        _selectPreProcessor: function(e) {
            this.$jsPreprocessor.val(e)
        },
        _addProcessorClassBoxJS: function(e) {
            this.$boxJS.data("preprocessor", __preprocessors.js.pretty_syntaxes[e]), this.$boxJS.removeClass(__preprocessors.js.syntaxes.join(" ")).addClass(e)
        },
        _addModuleClassBoxJS: function(e) {
            this.$boxJS.removeClass("module"), e && this.$boxJS.addClass("module")
        }
    });
! function() {
    function e(e, t) {
        return t.match("." + e + "$")
    }

    function t(e) {
        var t = e.split("/");
        return t.length > 0 ? t[t.length - 1].replace(".min.", ".") : e.replace(".min.", ".")
    }

    function n(n, s) {
        for (var i = [], o = {}, r = 0, c = s.length; r < c; r++)
            for (var u = s[r], l = u.latest.split(u.version)[0], d = u.assets[0].version, h = u.assets[0].files, p = 0; p < h.length; p++) {
                var f = h[p],
                    _ = t(f);
                if (e(n, _) && !o[_] && (o[_] = _, i.push({
                        name: _,
                        latest: l + d + "/" + f,
                        keywords: u.keywords
                    }), i.length >= a)) return i
            }
        return i
    }

    function s(e, t) {
        var s = n(e, t.results),
            i = $("#external-" + e + "-search").val();
        return TypeaheadSource.sortedMatches(i, s)
    }

    function i(e, t, n) {
        CacheGet.find({
            key: e + t.url,
            url: t.url,
            dataType: "json",
            expire: r,
            onSuccess: n,
            createCacheableResult: function(t) {
                return s(e, t)
            }
        })
    }

    function o(e) {
        return new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            identify: function(t) {
                return e + t.name
            },
            remote: {
                url: "https://api.cdnjs.com/libraries?fields=version,assets,keywords&search=%QUERY",
                cache: !1,
                wildcard: "%QUERY",
                transport: function(t, n) {
                    i(e, t, n)
                }
            }
        })
    }
    var r = 4320,
        a = 200;
    window.ResourcesBH = {
        getInstance: o
    }
}();
var ResourcesController = Class.extend({
    init: function(e) {
        this.type = e, this.events = new ResourcesEvents(this, e), this.view = new ResourcesView(e)
    },
    setItemValueFromServer: function(e) {
        CP.pen.setPenResources(e)
    },
    addEmptyResource: function(e) {
        CP.pen.addEmptyResource(e)
    },
    quickAddResource: function(e, t, n) {
        CP.pen.quickAddResource(e, t), CP.PenRecentResourcesManager.addNewPenResource(e, t, n)
    },
    deleteResource: function(e) {
        CP.pen.deleteResource(e)
    },
    setResource: function(e, t) {
        CP.pen.setResource(e, t)
    },
    updateResourcesOrder: function(e, t) {
        CP.pen.updateResourcesOrder(e, t)
    }
});
window.ResourcesEvents = Class.extend({
    init: function(e, t) {
        this.controller = e, this.type = t, _.extend(this, EnableDisableDriver), this.bindToEnableDisableHubEvents(), this._bindToDOM(), this._bindToHub()
    },
    _bindToDOM: function() {
        $("#add-" + this.type + "-resource")._on("click", this._onAddResourceClick, this), $("#" + this.type + "-quick-add")._on("change", this._onQuickAddChange, this), $("#" + this.type + "-external-resources").on("click", "span.remove-external-url", $.proxy(this._onDeleteResource, this)).on("keydown", this._getResourcesSelector(), $.proxy(this._onChangeResource, this)).on("change", this._getResourcesSelector(), $.proxy(this._onChangeResource, this)), $("#external-" + this.type + "-resources .recent-searches").on("click", "span", $.proxy(this._onQuickAddRecentClick, this)), window.__mobile || (this._makeResourcesSortable(), this.$searchInput = $("#external-" + this.type + "-search"), this._bindToTypeahead())
    },
    _getResourcesSelector: function() {
        return "input." + this.type + "-resource[name='external-" + this.type + "']"
    },
    _bindToHub: function() {
        Hub.sub("server-pen-change", $.proxy(this._onServerPenChange, this))
    },
    _onServerPenChange: function(e, t) {
        ObjectUtil.hasNestedValue(t, "pen.resources") && this.controller.setItemValueFromServer(t)
    },
    _makeResourcesSortable: function() {
        var e = this;
        $("#" + this.type + "-external-resources").sortable({
            handle: ".move-external-url",
            axis: "y",
            update: function() {
                e._updateResourcesOrder()
            }
        })
    },
    _updateResourcesOrder: function() {
        this._canDrive && this.controller.updateResourcesOrder(this.type, this._getViewIDsToOrders())
    },
    _checkResourcesSecurity: function(e) {
        var t = $("#external-resource-input-" + e),
            n = t.closest(".external-resource-url-row");
        "http:" === t.val().substr(0, 5) ? n.addClass("insecure-resource") : n.removeClass("insecure-resource")
    },
    _getViewIDsToOrders: function() {
        return _.reduce(this._getInputsByType(), function(e, t, n) {
            return e[this._getElementViewID(t)] = n, e
        }, {}, this)
    },
    _getInputsByType: function() {
        return _.select($("#" + this.type + "-external-resources input"), function(e) {
            return $(e).attr("id")
        })
    },
    _onChangeResource: function(e) {
        if (this._canDrive) {
            var t = this._getElementViewID(e.target);
            this.controller.setResource(t, $(e.target).val());
            var n = $(".open-external-url[data-view-id=" + t + "]");
            n.attr("href", $(e.target).val()), this._updateResourcesOrder();
            var s = this;
            setTimeout(function() {
                s._checkResourcesSecurity(t)
            }, 0)
        }
    },
    _onAddResourceClick: function() {
        this._canDrive && this.controller.addEmptyResource(this.type)
    },
    _onQuickAddChange: function(e, t) {
        this._canDrive && (this.controller.quickAddResource(this.type, t.val(), $("option:selected", t).text()), this._updateResourcesOrder())
    },
    _onQuickAddRecentClick: function(e) {
        var t = e.target;
        this._canDrive && (this.controller.quickAddResource(this.type, $(t).attr("data-val"), $(t).text()), this._updateResourcesOrder())
    },
    _onDeleteResource: function(e) {
        return e.preventDefault(), this._canDrive && (this.controller.deleteResource(this._getElementViewID(e.target)), this._updateResourcesOrder()), !1
    },
    _getElementViewID: function(e) {
        return $(e).attr("data-view-id")
    },
    _getAllUIElements: function() {
        return [$("#add-" + this.type + "-resource"), $("#" + this.type + "-quick-add"), $(this._getResourcesSelector())]
    },
    _bindToTypeahead: function() {
        var e = this,
            t = $(this.$searchInput);
        t.typeahead(this._getTypeaheadConfiguration(), this._getTypeaheadDatasource()), t.on("typeahead:change", function() {
            t.val("")
        }), t.on("typeahead:select", function(n, s) {
            e.controller.quickAddResource(e.type, s.latest, s.name), e._updateResourcesOrder(), setTimeout(function() {
                t.val("")
            }, 250)
        }), t.on("typeahead:asyncrequest", function() {
            $(".clock-spinner").addClass("show")
        }), t.on("typeahead:asynccancel typeahead:asyncreceive", function() {
            $(".clock-spinner").removeClass("show")
        })
    },
    _getTypeaheadConfiguration: function() {
        return {
            hint: !0,
            minLength: 3,
            highlight: !0
        }
    },
    _getTypeaheadDatasource: function() {
        return {
            name: this.type + "-search",
            source: ResourcesBH.getInstance(this.type),
            limit: 15,
            templates: {
                empty: function() {
                    return '<div class="typeahead-no-results-found">No results found.</div>'
                },
                suggestion: function(e) {
                    return "<p>" + e.name + "<small>" + e.latest + "</small></p>"
                }
            },
            display: function(e) {
                return e.latest
            }
        }
    }
}), window.ResourcesView = Class.extend({
    $resources: null,
    $quickAdd: null,
    init: function(e) {
        this.type = e, this._initDOM(), this._bindToHub()
    },
    _initDOM: function() {
        this.$resources = $("#" + this.type + "-external-resources"), this.$quickAdd = $("#" + this.type + "-quick-add"), this._syncExternalInputs(CP.pen.getResourcesByType(this.type)), this._appendRecentResourcesToUI(CP.PenRecentResourcesManager.getRecentPenResources(this.type))
    },
    _bindToHub: function() {
        Hub.sub("pen-change", $.proxy(this._onPenChange, this))
    },
    _onPenChange: function(e, t) {
        ObjectUtil.hasNestedValue(t, "pen.resources") && this._syncExternalInputs(CP.pen.getResourcesByType(this.type), t.rebind)
    },
    _syncExternalInputs: function(e) {
        this._syncNumberOfDOMElementsWithResources(e), this._syncTheDOMValuesWithResources(e), this._resetQuickAdd()
    },
    _syncNumberOfDOMElementsWithResources: function(e) {
        var t = this._getExistingExternalsDivs(),
            n = e.length - t.length;
        if (n > 0)
            for (; n--;) {
                var s = e.length - 1 - n;
                this._appendResourcesToUI(e[s], s)
            } else if (n < 0)
            for (n *= -1; n--;) $(t.last()).remove()
    },
    _syncTheDOMValuesWithResources: function(e) {
        _.forEach(this._getExistingExternalsDivs(), function(t, n) {
            t = $(t);
            var s = e[n];
            if (s.view_id === t.attr("data-view-id")) {
                var i = $("#external-resource-input-" + s.view_id);
                s.url !== i.val() && (i.removeClass("yellow-flash"), i.val(s.url), setTimeout(function() {
                    i.addClass("yellow-flash")
                }, 10))
            } else t.replaceWith(this._divHTML(s))
        }, this)
    },
    _getExistingExternalsDivs: function() {
        return $("#" + this.type + "-external-resources > div")
    },
    _appendResourcesToUI: function(e, t) {
        var n = $(this._divHTML(e, t)).find(".external-resource").addClass("yellow-flash").end();
        this.$resources.append(n)
    },
    _appendRecentResourcesToUI: function(e) {
        this._addRecentResourcesToClickBar(e), this._addRecentResourcesToDropdown(e)
    },
    _addRecentResourcesToClickBar: function(e) {
        var t = $("#external-" + this.type + "-resources .recent-searches");
        if (e.length > 0) {
            t.append($("<strong>Recent: </strong>"));
            for (var n = 0; n < e.length; n++) t.append($('<span class="quickadd-click" data-val="' + e[n].url + '">' + e[n].name + "</span>"))
        }
    },
    _addRecentResourcesToDropdown: function(e) {
        var t = $("#" + this.type + "-quick-add"),
            n = $(".recent-resources", t),
            s = $(".popular-resources", t);
        if (e.length > 0)
            for (var i = 0; i < e.length; i++) $('option[value="' + e[i].url + '"]', s).remove(), n.append($('<option value="' + e[i].url + '" label="' + e[i].name + '">' + e[i].name + "</option>"))
    },
    _divHTML: function(e, t) {
        return _.template(this._getTemplate(), {
            url: e.url,
            insecure_resource: "http:" === e.url.substr(0, 5) ? "insecure-resource" : "",
            view_id: e.view_id,
            placeholder: this._getPlaceholderForInput(e.resource_type, t)
        })
    },
    _template: "",
    _getTemplate: function() {
        return "" === this._template && (this._template = $("#" + this.type + "-external-resources-template").html()), this._template
    },
    _getPlaceholderForInput: function(e, t) {
        return t % 2 === 0 ? "css" === e ? "https://yourwebsite.com/style.css" : "https://yourwebsite.com/script.js" : "https://codepen.io/username/pen/aBcDef"
    },
    _resetQuickAdd: function() {
        this.$quickAdd.prop("selectedIndex", 0)
    }
});
var TypeaheadSource = {
        MAX_RESULTS: 50,
        sortedMatches: function(e, t) {
            return this._sort(e, this._findMatches(e.toLowerCase(), t)).splice(0, this.MAX_RESULTS)
        },
        _findMatches: function(e, t) {
            return _.filter(t, function(t) {
                return t.name.toLowerCase().indexOf(e) > -1 || !!(t.tokens && t.tokens.indexOf(e) > -1)
            })
        },
        _sort: function(e, t) {
            return t.sort(function(t, n) {
                var s = t.name.toLowerCase(),
                    i = n.name.toLowerCase();
                if (s === e && i !== e) return -1;
                if (s !== e && i === e) return 1;
                var o = t.name.indexOf(e),
                    r = n.name.indexOf(e);
                if (o > -1 && r === -1) return -1;
                if (o === -1 && r > -1) return 1;
                if (o > -1 && r > -1) return o < r ? -1 : o > r ? 1 : s.length - e.length < i.length - e.length ? -1 : 1;
                var a = t.keywords.indexOf(e),
                    c = n.keywords.indexOf(e);
                return a > -1 && c === -1 ? -1 : a === -1 && c > -1 ? 1 : 0
            })
        }
    },
    SearchFilter = {
        init: function() {
            this._bindToDOM()
        },
        _bindToDOM: function() {
            $("#assets-search")._on("keyup click search", this._onSearchChange, this, !0)
        },
        _onSearchChange: function(e, t) {
            var n = $.trim(t.val().toLowerCase());
            if ("" === n) $(".single-asset").removeClass("hide");
            else {
                $(".single-asset").addClass("hide");
                var s = ".single-asset[data-searchable-name*='" + n + "']";
                $(s).closest(".single-asset").removeClass("hide")
            }
        }
    };
! function() {
    function e(e, t) {
        return {
            asset_type: "account",
            name: t,
            size: e.size,
            content_type: e.type
        }
    }

    function t(e, t, n) {
        AJAXUtil.post("/uploaded_assets/asset_upload_request", e, function(e) {
            var s = new FormData;
            for (var i in e.form_data.fields) s.append(i, e.form_data.fields[i]);
            s.append("file", t), $.ajax({
                url: e.form_data.action,
                data: s,
                processData: !1,
                contentType: !1,
                type: "POST",
                success: function(t, s) {
                    n(e, s)
                }
            })
        })
    }

    function n(n, s, i) {
        t(e(n, s), n, function(e, t) {
            i("success" === t ? {
                success: !0,
                file: n,
                fileName: s
            } : {
                success: !1,
                file: n,
                error: t.text
            })
        })
    }

    function s(e, t, n) {
        var s = {
            asset_type: "avatar",
            file_ext: e.toLowerCase(),
            content_type: t
        };
        return n && (s.team = !0), s
    }

    function i(e, t, n) {
        return {
            asset_type: "screenshot",
            item_type: n,
            item_id: CP.pen.id,
            file_ext: e.toLowerCase(),
            content_type: t
        }
    }

    function o(e, n, i, o, r) {
        t(s(i, e.type, o), n, function(e, t) {
            var n = e.form_data.action + "/" + e.form_data.fields.key;
            r("success" === t ? {
                success: !0,
                url: n
            } : {
                success: !1,
                error: t.text
            })
        })
    }

    function r(e, n, s, o, r) {
        t(i(o, n.type, e), s, function(e, t) {
            var n = e.form_data.action + "/" + e.form_data.fields.key;
            r("success" === t ? {
                success: !0,
                url: n
            } : {
                success: !1,
                error: t.text
            })
        })
    }
    window.S3 = window.S3 || {}, window.S3.uploadAvatarAssetToS3 = o, window.S3.uploadAccountAssetFileToS3 = n, window.S3.uploadScreenshotAssetToS3 = r
}(), jQuery.event.props.push("dataTransfer");
var ScreenshotUpload = {
    screenshotWrap: $("#settings-screenshot-wrap"),
    imageChanger: $("#screenshot-image-changer"),
    imageHolder: $("#custom-screenshot"),
    fileInput: $("#personal-profile-image-upload-input"),
    theBody: $("body"),
    deleteBtn: $("#delete-screenshot"),
    init: function() {
        _.extend(this, AJAXUtil), this.bindUIActions()
    },
    bindUIActions: function() {
        var e = this;
        this.uploading = this.screenshotWrap.find(".uploading-message"), this.imageChanger._on("drop", function(e) {
            e.preventDefault(), ScreenshotUpload.handleDrop(e)
        }), this.theBody._on("dragover", function(e) {
            e.currentTarget === ScreenshotUpload.theBody[0] && ScreenshotUpload.testIfContainsFiles(e) && ScreenshotUpload.highlightDropArea()
        }), this.theBody._on("dragleave", function(e) {
            e.currentTarget === ScreenshotUpload.theBody[0] && ScreenshotUpload.unHighlightDropArea()
        }), ScreenshotUpload.fileInput._on("change", function(e) {
            ScreenshotUpload.handleManualFileSelect(e)
        }), this.screenshotWrap.on("click", "#delete-screenshot", function(t) {
            t.preventDefault(), $.showModal("/ajax/confirm_custom_screenshot_delete", "modal-warning", function() {
                $("#confirm-delete-screenshot")._on("click", function(t) {
                    t.preventDefault(), e.deleteScreenshot()
                })
            })
        })
    },
    testIfContainsFiles: function(e) {
        if (e.dataTransfer.types)
            for (var t = 0; t < e.dataTransfer.types.length; t++)
                if ("Files" === e.dataTransfer.types[t]) return !0;
        return !1
    },
    highlightDropArea: function() {
        ScreenshotUpload.imageChanger.addClass("drop-here")
    },
    unHighlightDropArea: function() {
        ScreenshotUpload.imageChanger.removeClass("drop-here")
    },
    handleDrop: function(e) {
        var t = e.dataTransfer.files;
        ScreenshotUpload.processFile(t)
    },
    handleManualFileSelect: function(e) {
        var t = e.target.files;
        ScreenshotUpload.processFile(t)
    },
    processFile: function(e) {
        this.unHighlightDropArea(), this._showUploadingMessage(!0);
        var t = e[0];
        if (t.type.match("image.*")) {
            var n = this;
            this.readImage(t, function(e) {
                "string" != typeof e ? $.showMessage("Oops! We couldn't upload that image. Try again.") : (n._showSavingMessage(), n._signScreenshotAndUpload(e, t))
            })
        } else $.showMessage("Oops! That file wasn't a JPG or PNG."), ScreenshotUpload.unHighlightDropArea()
    },
    _showSavingMessage: function() {
        $.showMessage("Saving Screenshot...")
    },
    _showSavedMessage: function() {
        $.showMessage("Pen Screenshot is saved. It may take a minute to update everywhere.")
    },
    _signScreenshotAndUpload: function(e, t) {
        var n = this.dataURItoBlob(e),
            s = this.fileExtension(t.name),
            i = this;
        S3.uploadScreenshotAssetToS3("pen", t, n, s, function(e) {
            e.success ? setTimeout(function() {
                i.saveScreenshot(e.url)
            }, 500) : $.showMessage("Oops! We couldn't upload that image. Try again.")
        })
    },
    fileExtension: function(e) {
        return e.split(".").pop()
    },
    dataURItoBlob: function(e) {
        for (var t = atob(e.split(",")[1]), n = new ArrayBuffer(t.length), s = new Uint8Array(n), i = 0; i < t.length; i++) s[i] = t.charCodeAt(i);
        return new Blob([n], {
            type: "image/jpeg"
        })
    },
    readImage: function(e, t) {
        var n = new FileReader;
        n.onload = function() {
            t(this.result)
        }, n.readAsDataURL(e), n.onabort = function() {
            $.showMessage("The upload was aborted.")
        }, n.onerror = function() {
            $.showMessage("An error occurred while reading the file.")
        }
    },
    placeScreenshot: function(e, t) {
        this.screenshotWrap.html(t), $("#custom-screenshot").css("background-image", "url(" + e + "?" + Date.now() + ")"), this.deleteBtn.show(), this.uploading = this.screenshotWrap.find(".uploading-message")
    },
    removeScreenshot: function(e, t) {
        this.screenshotWrap.html(t), this.deleteBtn.hide(), this.uploading = this.screenshotWrap.find(".uploading-message")
    },
    saveScreenshot: function(e) {
        var t = this,
            n = {
                item_type: "pen",
                item_id: CP.pen.slug_hash
            };
        null === CP.pen.custom_screenshot ? this.post("/custom_screenshot", n, function(n) {
            t._successfulUpdate(n, e)
        }) : this.put("/custom_screenshot/" + CP.pen.custom_screenshot, n, function(n) {
            t._successfulUpdate(n, e)
        })
    },
    _successfulUpdate: function(e, t) {
        CP.pen.custom_screenshot = e.screenshot_hashid, this._showSavedMessage(), this.placeScreenshot(t, e.description_html), this._showUploadingMessage(!1)
    },
    deleteScreenshot: function() {
        this.del("/custom_screenshot/" + CP.pen.custom_screenshot, {
            item_type: "pen",
            item_id: CP.pen.slug_hash
        }, function(e) {
            $.hideModal(), this.removeScreenshot(e.screenshot_url_large, e.description_html), CP.pen.custom_screenshot = null
        })
    },
    _showUploadingMessage: function(e) {
        e ? this.uploading.css({
            opacity: 1,
            visibility: "visible"
        }) : this.uploading.css({
            opacity: 0,
            visibility: "hidden"
        })
    }
};
! function() {
    ScreenshotUpload.init()
}();
var SettingsController = Class.extend({
    popupName: "penSettings",
    init: function() {
        CP.SettingsEvents.init(this), this.model = CP.SettingsModel, this.model.init(this), CP.SettingsView.init(this.model)
    },
    syncWithServer: function(e) {
        this.model.syncWithServer(e)
    },
    toggleSettingsPane: function() {
        this.model.toggleSettingsPane()
    },
    hideSettingsPane: function() {
        this.model.hideSettingsPane()
    },
    selectSettingsTab: function(e) {
        this.model.selectSettingsTab(e)
    },
    settingsPaneVisible: function() {
        return this.model.settingsPaneVisible()
    }
});
! function() {
    function e() {
        f._on("click", t, window), g._on("click", n, window, !0), y._on("click", s, window), P._on("click", i, window), $("#item-settings-modal .tabs > nav a").unbind(), m._on("click", o, window), $("body")._on("click", r, window, !0), v._on("click", a, window)
    }

    function t(e, t) {
        CP.SettingsEvents._canDrive && (n(), p.selectSettingsTab(t.data("type")))
    }

    function n() {
        CP.SettingsEvents._canDrive && p.toggleSettingsPane()
    }

    function s() {
        n(), setTimeout(function() {
            $("#item-details-description").focus()
        }, 600)
    }

    function i() {
        n(), setTimeout(function() {
            $("#pen-tags").focus(), $("#project-tags").focus()
        }, 600)
    }

    function o(e) {
        if (CP.SettingsEvents._canDrive) {
            var t = $(e.target).data("type");
            t && p.selectSettingsTab(t)
        }
    }

    function r(e) {
        if (b.length) {
            var t = b[0].contains(e.target);
            g.length & !t && (t = g[0].contains(e.target)), t || p.hideSettingsPane()
        }
    }

    function a() {
        CP.SettingsEvents._canDrive && p.hideSettingsPane()
    }

    function c() {
        Hub.sub("key", d), Hub.sub("pen-saved", l), Hub.sub("server-ui-change", u), Hub.sub("popup-open", h)
    }

    function u(e, t) {
        (ObjectUtil.hasNestedValue(t, "ui.settings.pane") || ObjectUtil.hasNestedValue(t, "ui.settings.tab")) && p.syncWithServer(t)
    }

    function l() {
        p.hideSettingsPane()
    }

    function d(e, t) {
        "esc" === t.key && p.hideSettingsPane()
    }

    function h(e, t) {
        t !== p.popupName && p.hideSettingsPane()
    }
    var p, f = $("button.settings-nub"),
        g = $("#edit-settings"),
        m = $("#settings-tabs"),
        v = $("#close-settings"),
        C = $("#popup-overlay"),
        b = $("#item-settings-modal"),
        y = $(".edit-details-reminder"),
        P = $(".edit-tags-reminder");
    CP.SettingsEvents = {}, _.extend(CP.SettingsEvents, EnableDisableDriver), CP.SettingsEvents._getAllUIElements = function() {
        return [f, g, m, C, v]
    }, CP.SettingsEvents.init = function(t) {
        p = t, this.bindToEnableDisableHubEvents(), e(), c()
    }
}(),
    function() {
        function e() {
            Hub.pub("ui-change", CP.SettingsModel.getState())
        }

        function t(t) {
            CP.ui && CP.ui.settings.pane !== t && (CP.ui.settings.pane = t, e(), "open" === t && Hub.pub("popup-open", n.popupName))
        }
        var n;
        CP.SettingsModel = {
            init: function(e) {
                n = e
            },
            syncWithServer: function(t) {
                CP.ui.settings.tab = t.ui.settings.tab, CP.ui.settings.pane = t.ui.settings.pane, e()
            },
            toggleSettingsPane: function() {
                var e = this.settingsPaneVisible() ? "closed" : "open";
                t(e)
            },
            hideSettingsPane: function() {
                t("closed")
            },
            openSettingsPane: function() {
                t("open")
            },
            selectSettingsTab: function(t) {
                CP.ui.settings.tab = t, e()
            },
            getState: function() {
                return {
                    ui: {
                        settings: {
                            pane: CP.ui.settings.pane,
                            tab: CP.ui.settings.tab
                        }
                    }
                }
            },
            settingsPaneVisible: function() {
                return "open" === CP.ui.settings.pane
            }
        }
    }(),
    function() {
        function e() {
            Hub.sub("ui-change", t)
        }

        function t(e, t) {
            ObjectUtil.hasNestedValue(t, "ui.settings.pane") && n(t.ui.settings.pane), ObjectUtil.hasNestedValue(t, "ui.settings.tab") && r(t.ui.settings)
        }

        function n(e) {
            "open" === e ? s() : i()
        }

        function s() {
            a || (c.addClass("open"), CP.showPopupOverlay(), o(), a = !0)
        }

        function i() {
            a && (c.removeClass("open"), CP.hidePopupOverlay(), a = !1)
        }

        function o() {
            window.__mobile || setTimeout(function() {
                d.focus()
            }, 500)
        }

        function r(e) {
            u.removeClass("active"), l.removeClass("active"), $("#settings-" + e.tab + "-tab").addClass("active"), $("#settings-" + e.tab).addClass("active")
        }
        var a = !1,
            c = $("#item-settings-modal"),
            u = $("#settings-tabs a"),
            l = $("div.settings"),
            d = $("#item-details-title");
        CP.SettingsView = {}, CP.SettingsView.init = function(n) {
            t(null, n.getState()), e()
        }
    }();
var PenTagsController = Class.extend({
        init: function() {
            this.model = new PenTagsModel, this.events = new PenTagsEvents(this), this.view = new PenTagsView(this.model)
        },
        addNewTags: function(e) {
            this.model.addNewTags(e)
        },
        deleteTag: function(e) {
            this.model.deleteTag(e)
        }
    }),
    PenTagsEvents = Class.extend({
        $body: $("body"),
        $penTags: $("#pen-tags"),
        init: function(e) {
            this.controller = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            this.$body.on("click", "#active-tags span span.tag-x", $.proxy(this._handleTagDelete, this)), this.$penTags._on("keyup", this._onTagChange, this)
        },
        _onTagChange: function() {
            this.controller.addNewTags(this.$penTags.val())
        },
        _handleTagDelete: function(e) {
            return e.preventDefault(), this.controller.deleteTag(this._getTagToDelete(e)), !1
        },
        _getTagToDelete: function(e) {
            var t = $(e.target);
            return $.trim(t.next().html())
        }
    }),
    PenTagsModel = Class.extend({
        init: function() {},
        getTags: function() {
            return CP.pen.getTags()
        },
        addNewTags: function(e) {
            CP.item.setItemValue({
                origin: "client",
                pen: {
                    newTags: this._validNewTags(e)
                }
            })
        },
        _validNewTags: function(e) {
            return _.uniq(_.map(e.split(","), function(e) {
                return _stripHTMLTags($.trim(e).toLowerCase()).replace(/(&|#)/g, "")
            }))
        },
        deleteTag: function(e) {
            CP.item.setItemValue({
                origin: "client",
                action: "delete-tag",
                pen: {
                    tags: _.without(CP.pen.tags, e)
                }
            }), CP.item.setItemValue({
                origin: "client",
                action: "delete-tag",
                pen: {
                    newTags: _.without(CP.pen.newTags, e)
                }
            })
        }
    }),
    PenTagsView = Class.extend({
        MAX_TAGS: 5,
        $activeTags: $("#active-tags"),
        $maxTagsLabel: $("#max-tags-label"),
        $penTags: $("#pen-tags"),
        init: function(e) {
            this.model = e, this._initiateView(), this._bindToHub()
        },
        _initiateView: function() {
            this._updateActiveTagsHTML(this.model.getTags())
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this)), Hub.sub("pen-saved", $.proxy(this._onPenSaved, this))
        },
        _onPenSaved: function() {
            this.$penTags.val("")
        },
        _onPenChange: function(e, t) {
            "pen" in t && ("tags" in t.pen && this._updateActiveTagsHTML(this.model.getTags()), "newTags" in t.pen && (this._updatePenTagsVal(t), this._updateActiveTagsHTML(this.model.getTags())))
        },
        _updateActiveTagsHTML: function(e) {
            this.$activeTags.html(this._getTagsHTML(e)), this._warnAboutTooManyTags(e)
        },
        _getTagsHTML: function(e) {
            for (var t = "", n = 0; n < e.length; n++) e[n] && (t += "<span class='tag'>", t += "<span class='tag-x' style='cursor:pointer;'>", t += "<span class='tag-x'>\xd7</span>&nbsp;", t += "<span class='tag-name'>" + e[n] + "</span> ", t += "</span> ", t += "</span>");
            return t
        },
        _updatePenTagsVal: function(e) {
            "delete-tag" === e.action && this.$penTags.val(e.pen.newTags.join(","))
        },
        _warnAboutTooManyTags: function(e) {
            e.length >= this.MAX_TAGS ? this.$maxTagsLabel.addClass("at-capacity") : this.$maxTagsLabel.removeClass("at-capacity")
        }
    }),
    ProjectTagsController = Class.extend({
        init: function() {
            this.model = new ProjectTagsModel, this.events = new ProjectTagsEvents(this), this.view = new ProjectTagsView(this.model)
        },
        addNewTags: function(e) {
            this.model.addNewTags(e), this.view.updateTags()
        },
        deleteTag: function(e) {
            this.model.deleteTag(e)
        }
    }),
    ProjectTagsEvents = Class.extend({
        $body: $("body"),
        $projectTags: $("#project-tags"),
        init: function(e) {
            this.controller = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            this.$body.on("click", "#active-tags span span.tag-x", $.proxy(this._handleTagDelete, this)), this.$projectTags._on("keyup", this._onTagChange, this)
        },
        _onTagChange: function() {
            this.controller.addNewTags(this.$projectTags.val())
        },
        _handleTagDelete: function(e) {
            return e.preventDefault(), this.controller.deleteTag(this._getTagToDelete(e)), !1
        },
        _getTagToDelete: function(e) {
            var t = $(e.target);
            return $.trim(t.next().html())
        }
    }),
    ProjectTagsModel = Class.extend({
        init: function() {},
        getTags: function() {
            return CP.item.getTags()
        },
        addNewTags: function(e) {
            CP.item.setItemValue({
                origin: "client",
                item: {
                    newTags: this._validNewTags(e)
                }
            })
        },
        _validNewTags: function(e) {
            return _.uniq(_.map(e.split(","), function(e) {
                return _stripHTMLTags($.trim(e).toLowerCase()).replace("&", "")
            }))
        },
        deleteTag: function(e) {
            CP.item.setItemValue({
                origin: "client",
                action: "delete-tag",
                item: {
                    tags: _.without(CP.item.tags, e)
                }
            }), CP.item.setItemValue({
                origin: "client",
                action: "delete-tag",
                item: {
                    newTags: _.without(CP.item.newTags, e)
                }
            })
        }
    }),
    ProjectTagsView = Class.extend({
        MAX_TAGS: 5,
        $activeTags: $("#active-tags"),
        $maxTagsLabel: $("#max-tags-label"),
        $projectTags: $("#project-tags"),
        init: function(e) {
            this.model = e, this._initiateView()
        },
        _initiateView: function() {
            this._updateActiveTagsHTML(this.model.getTags())
        },
        _onProjectSaved: function() {
            this.$projectTags.val("")
        },
        updateTags: function() {
            this._updateActiveTagsHTML(this.model.getTags()), this._updateProjectTagsVal(CP.item.newTags), this._updateActiveTagsHTML(this.model.getTags())
        },
        _updateActiveTagsHTML: function(e) {
            this.$activeTags.html(this._getTagsHTML(e)), this._warnAboutTooManyTags(e)
        },
        _getTagsHTML: function(e) {
            for (var t = "", n = 0; n < e.length; n++) e[n] && (t += '<span class="tag">', t += '<span class="tag-x" style="cursor:pointer;">', t += '<span class="tag-x">\xd7</span>&nbsp;', t += '<span class="tag-name">' + e[n] + "</span> ", t += "</span> ", t += "</span>");
            return t
        },
        _updateProjectTagsVal: function(e) {
            "delete-tag" === e.action && this.$projectTags.val(e.project.newTags.join(","))
        },
        _warnAboutTooManyTags: function(e) {
            e.length >= this.MAX_TAGS ? this.$maxTagsLabel.addClass("at-capacity") : this.$maxTagsLabel.removeClass("at-capacity")
        }
    }),
    ShareGist = Class.extend({
        WAITING_MSG_TIMEOUT: 1e4,
        GIST_MSG_TIMEOUT: 7e3,
        init: function(e) {
            _.extend(this, AJAXUtil), this._shareView = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            $("#share-gist")._on("click", $.proxy(this._createGist, this))
        },
        _createGist: function() {
            $.showMessage(Copy.waitingForGist, this.WAITING_MSG_TIMEOUT), this.post("/share/gist.json", {
                data: JSON.stringify(CP.pen)
            }, this._doneCreateGist)
        },
        _doneCreateGist: function(e) {
            if (e.reauth && $.showModal("/ajax/reauthenticate_github", "modal-error", function() {
                    $("#cancel-reauth")._on("click", function() {
                        $.hideModal()
                    })
                }), e.url) {
                var t = _.template(Copy.gistCreated, {
                    url: e.url
                });
                $.showMessage(t, this.GIST_MSG_TIMEOUT), window.open(e.url)
            }
        }
    }),
    ShareSMS = Class.extend({
        $sendToPhone: $("#send-to-phone"),
        $sendToPhoneForm: $("#send-to-phone-form"),
        $sendButton: $("#sms-send-button"),
        $smsPhone: $("#sms-phone"),
        $textsLeft: $("#texts-left"),
        init: function(e) {
            _.extend(this, AJAXUtil), this._shareView = e, this._bindToDOM(), this._loadSMSPhoneNumberFromCookie()
        },
        _bindToDOM: function() {
            this.$sendToPhoneForm._on("submit", this.sendSMS, this)
        },
        _loadSMSPhoneNumberFromCookie: function() {
            $.cookie("sms_phone_number") && this.$sendToPhone.val($.cookie("sms_phone_number"))
        },
        sendSMS: function() {
            this.$sendButton.prop("disabled", !0);
            var e = {
                phone_number: this.$sendToPhone.val(),
                item_type: "pen",
                item_id: CP.pen.getActiveSlugHash()
            };
            this.post("/share/sms/", e, this._doneSendSMS, this._failedSendSMS)
        },
        _doneSendSMS: function(e) {
            this._clearSMSErrors(), this._setSMSPhoneNumberCookie(e), this.$sendButton.prop("disabled", !1), this.$textsLeft.html(e.sms_left), $.showMessage(_.template(Copy.smsSentTo, {
                phone_number: e.phone_number
            }), "slow")
        },
        _setSMSPhoneNumberCookie: function(e) {
            var t = {
                expires: 30,
                domain: document.location.host,
                path: "/"
            };
            $.cookie("sms_phone_number", e.phone_number, t)
        },
        _failedSendSMS: function(e) {
            this._clearSMSErrors(), this.$sendButton.prop("disabled", !1), this.$smsPhone.addClass("error"), this.$smsPhone.append(this._getErrorsHTML(e))
        },
        _getErrorsHTML: function(e) {
            var t = "";
            for (var n in e.errors) t += "<div class='error-message'>" + e.errors[n] + "</div>";
            return t
        },
        _clearSMSErrors: function() {
            this.$smsPhone.removeClass("error"), $(".error-message").remove()
        }
    }),
    ShareView = Class.extend({
        $downloadExportIFrame: null,
        init: function() {
            _.extend(this, AJAXUtil), this.shareSMS = new ShareSMS(this), this.shareGist = new ShareGist(this), this._bindToDOM(), this._bindToHub(), this._updateRoomLinks()
        },
        _bindToHub: function() {
            Hub.sub("pen-change", $.proxy(this._onPenChange, this))
        },
        _bindToDOM: function() {
            $("#share-zip").on("click", $.proxy(this._onShareZipClick, this))
        },
        _onPenChange: function(e, t) {
            ObjectUtil.hasNestedValue(t, "pen.private") && this._updateRoomLinks()
        },
        _updateRoomLinks: function() {
            $("#editor-link").attr("href", this._getViewURL("pen")), $("#details-link").attr("href", this._getViewURL("details")), $("#full-page-link").attr("href", this._getViewURL("full")), $("#full-page-url").val(URLBuilder.getShortViewURL("", CP.pen)), $("#live-view-link").attr("href", this._getViewURL("live")), $("#live-view-url").val(URLBuilder.getShortViewURL("v", CP.pen));
            var e = this._getViewURL("collab");
            $("#collab-view-link").attr("href", e), $("#collab-link").attr("href", e), $("#collab-view-url").val(URLBuilder.getShortViewURL("c", CP.pen));
            var t = this._getViewURL("professor");
            $("#professor-view-link").attr("href", t), $("#professor-link").attr("href", t), $("#professor-view-url").val(URLBuilder.getShortViewURL("p", CP.pen)), $("#share-zip").attr("href", this._getViewURL("share/zip"))
        },
        _onShareZipClick: function(e) {
            e.stopPropagation();
            var t = $(e.target).attr("href");
            return this.$downloadExportIFrame ? this.$downloadExportIFrame.attr("src", t) : this.$downloadExportIFrame = $("<iframe>", {
                id: "downloadExportIFrame",
                src: t
            }).hide().appendTo("body"), !1
        },
        _getViewURL: function(e) {
            return URLBuilder.getViewURL(e, CP.profiled, CP.pen, !1)
        }
    }),
    Copy = {
        autoSavingNow: "Autosave enabled. <a href='https://blog.codepen.io/documentation/editor/autosave/' target='_blank'>?</a>",
        penUpdated: "Pen saved.",
        penForked: "We forked this Pen. It's saved to your account, but you can <a href='<%= url %>' target='_blank'>view it here.</a>",
        waitingForGist: "Creating GitHub Gist. Please be patient and stay awesome.",
        gistCreated: "Thanks for staying awesome. <a href='<%= url %>' target='_blank'> Here's a link to your Gist.</a>",
        youHaveUnsavedChanges: "You have NOT saved your Pen. Stop and save if you want to keep your Pen.",
        youHaveUnsavedSettings: "You have unsaved settings changes, are you sure you want to leave without saving?",
        collectionSavedPenAdded: "Your Collection '<%= name %>' was created and this Pen was added to it. <a href='<%= url %>'>View Collection</a>.",
        penAddToCollection: "This Pen was added to the '<%= name %>' Collection. <a href='<%= url %>'>View Collection</a>.",
        smsSentTo: "Text sent to phone <%= phone_number %>.",
        pauseAndPlay: "Pause and Play",
        catchUpToClass: "Catch Up to Class",
        studentWatching: "Student Watching",
        studentsWatching: "Students Watching",
        unsubcribedFromCommentNotifications: "You've been unsubscribed from comments for this Pen",
        subscribedToCommentNotifications: "You've been subscribed to comments for this Pen",
        deletingPen: "Deleting this Pen. Buckle up!",
        viewSource: "View Source",
        returnToSource: "Return to Source",
        errors: {
            "anon-cannot-save-during-rt-session": "Please login to save this Pen.",
            "unable-to-save-try-again": "Unable to save Pen. Trying again for the <%= time %> time. Please be patient.",
            "unable-to-save-ever": "Unable to save pen. Please contact support@codepen.io.",
            "disabled-cookies": "You will not be able to see live changes to a Pen while cookies are disabled",
            "pen-too-large": "This Pen is larger than the 1 megabyte limit. Try removing data from this Pen and trying again."
        }
    },
    CPFactory = {
        buildDataObjects: function() {
            CP.profiled = new Profiled, CP.user = new User, CP.itemType = window.__itemType, "pen" === CP.itemType ? (CP.pen = new Pen, CP.item = CP.pen, CP.penSaver = new PenSaver, CP.penAutosave = new PenAutosave, CP.ui = UI.buildDefaultUIData()) : "project" === CP.itemType && (CP.item = new Project, CP.ui = UI.buildDefaultUIData())
        },
        buildEditorObjects: function(e) {
            CP.penErrorHandler = new PenErrorHandler, CP.penResources = new PenResources, CP.penProcessor = new PenProcessor, CP.penRenderer = new PenRenderer, CP.codeEditorAnalayze = new CodeEditorAnalyze, CP.htmlEditor = new HTMLEditor("html", CP.pen.html), CP.cssEditor = new CSSEditor("css", CP.pen.css), CP.jsEditor = new JSEditor("js", CP.pen.js), e || CP.ConsoleEditor.init(), this.buildCommonEditorSettingsObjects(), this._buildSettingsObjects()
        },
        buildCommonEditorSettingsObjects: function() {
            CP.pen ? (CP.shareView = new ShareView, CP.settingsController = new SettingsController, CP.penActions = new PenActions, CP.infoController = new InfoController, CP.headerController.init(), CP.tagsController = new PenTagsController) : CP.item && "project" == CP.itemType && (CP.settingsController = new SettingsController, CP.projectActions = new ProjectActions, CP.infoController = new InfoController, CP.tagsController = new ProjectTagsController)
        },
        _buildSettingsObjects: function() {
            CP.htmlSettingsController = new HTMLSettingsController(CP.pen), CP.cssSettingsController = new CSSSettingsController(CP.pen), CP.jsSettingsController = new JSSettingsController(CP.pen), CP.behaviorController = new BehaviorController(CP.pen)
        },
        buildDesktopViewEditorObjects: function() {
            CP.codeEditorResizeController.init(), CP.editorDropDowns.init(), CP.codeEditorTidyController = new CodeEditorsTidyController, CP.codeEditorsCSSTransitionHandler = new CodeEditorsCSSTransitionHandler, CP.codeEditorsViewSourceController = new CodeEditorsViewSourceController, CP.keyBindings.init()
        }
    };
! function() {
    function e() {
        var e = document.location.href.replace(/&?bookmarklet_id=[^=]+/, "").replace(/\?$/, "");
        window.history.replaceState(e, "", e)
    }
    window.CP.cleanEditorURL = e
}();
var HandleIFrameClicks = {
        init: function(e) {
            this.pen = e, this._bindToDOM()
        },
        _bindToDOM: function() {
            _onMessage($.proxy(this.handleIFrameClickEvent, this))
        },
        handleIFrameClickEvent: function(e) {
            if (this._allowedToOpenWindows()) {
                var t = this._cleanURL(this._getURLFromEvent(e));
                t.match(/^https?:\/\/\S+$/) && window.open(t)
            }
        },
        _allowedToOpenWindows: function() {
            return this.pen.user_id > 1
        },
        _getURLFromEvent: function(e) {
            return "string" == typeof e.data ? e.data : ""
        },
        _cleanURL: function(e) {
            var t = this._getIFrameURLRemoved(e);
            return t = this._sanitizeURL(t), t = t.replace(/(java|vb)?script/gim, ""), t = t.replace(/eval/gim, ""), t = t.split("?")[0]
        },
        _getIFrameURLRemoved: function(e) {
            return e.replace(/http(s)?:\/\/(s\.)?codepen\.(dev|io)\/(boomerang\/\S+|\S+\/fullpage)\/\w+(\.html)?/m, "")
        },
        _sanitizeURL: function(e) {
            return e.replace(/[^-A-Za-z0-9+&@#\/%?=~_|!:,.;\(\)]/, "")
        }
    },
    TimeUtil = {
        countToString: function(e) {
            var t = {
                1: "first",
                2: "second",
                3: "third",
                4: "fourth",
                5: "fifth"
            };
            return e in t ? t[e] : e
        }
    },
    TypesUtil = {
        _HTML_TYPES: ["html", "xml", "haml", "markdown", "slim", "pug", "application/x-slim"],
        _CSS_TYPES: ["css", "less", "scss", "sass", "stylus", "text/css", "text/x-sass", "text/x-scss", "text/x-less", "text/x-styl"],
        _JS_TYPES: ["js", "javascript", "coffeescript", "livescript", "typescript", "text/javascript", "text/x-coffeescript", "text/x-livescript", "text/typescript"],
        cmModeToType: function(e) {
            var t = this._getSafeInputMode(e);
            return this._getType(t)
        },
        _getSafeInputMode: function(e) {
            var t = "string" == typeof e ? e : e.name;
            return t.toLowerCase()
        },
        syntaxToType: function(e) {
            return this._getType(e)
        },
        _getType: function(e) {
            return this._isHTMLType(e) ? "html" : this._isCSSType(e) ? "css" : this._isJSType(e) ? "js" : "unknown"
        },
        _isHTMLType: function(e) {
            return _.contains(this._HTML_TYPES, e)
        },
        _isCSSType: function(e) {
            return _.contains(this._CSS_TYPES, e)
        },
        _isJSType: function(e) {
            return _.contains(this._JS_TYPES, e)
        }
    };
if (NastyBrowserSniffing.ie() < 11) {
    var editorPage = window.location.href.indexOf("/pen") > -1 || window.location.href.indexOf("/project/editor") > -1 || window.location.href.indexOf("/project/live"),
        redirect = "/unsupported/";
    if (editorPage) {
        var initData = document.getElementById("init-data").getAttribute("value");
        "string" == typeof initData && (initData = JSON.parse(initData), initData.__debug_redirect && (redirect = "/unsupported?redirect=" + encodeURI(initData.__debug_redirect)))
    }
    window.location = redirect
}! function() {
    $(".history-toggle-button"), $(".history-close-button"), $(".history")
}(),
    function() {
        function e() {
            ne = $(window), se = $("body"), R = $("#result_div"), O = $("#resizer"), F = $("#editor-drag-cover"), L = $("#width-readout"), H = $(".boxes"), U = $(".top-boxes"), j = $(".output-sizer"), I = $(".box-console"), N = $(".page-wrap"), B = I.find(".close-editor-button"), V = $(".console-toggle-button")
        }

        function t() {
            ne.on("resize", s);
            var e = new BarDragger(O[0]);
            e.on("pointerDown", p), e.on("pointerUp", f), e.on("dragStart", g), e.on("dragMove", b), e.on("dragEnd", S), n($(".editor-resizer-console")[0]), n($(".box-console .powers")[0]), V.on("click", i), B.on("click", o)
        }

        function n(e) {
            if (e) {
                var t = new BarDragger(e);
                t.on("pointerDown", p), t.on("pointerUp", f), t.on("dragStart", T), t.on("dragMove", E), t.on("dragEnd", A), t.on("doubleClick", k)
            }
        }

        function s() {
            M()
        }

        function i(e) {
            e.preventDefault(), CP.EditorLayout && CP.EditorLayout.toggleConsole()
        }

        function o(e) {
            e.preventDefault(), CP.EditorLayout.closeConsole()
        }

        function r() {
            Hub.sub("ui-change", a)
        }

        function a(e, t) {
            var n = t.ui && t.ui.editorSizes && t.ui.editorSizes.console;
            void 0 !== n && c(n)
        }

        function c(e) {
            if ("closed" === e) return void l();
            if (0 !== q) {
                u();
                var t = Z / q,
                    n = e * (1 - t) + t;
                I.height(100 * n + "%"), ce = e
            }
        }

        function u() {
            re || (Hub.pub("console-opened"), I.show(), C(), re = !0)
        }

        function l() {
            re && (Hub.pub("console-closed"), I.hide(), re = !1)
        }

        function d(e) {
            Hub.pub("editor-sizes-change", {
                console: e
            })
        }

        function h() {
            var e = CP.ui.editorSizes.console;
            "closed" !== e && (u(), c(e))
        }

        function p() {
            F.show()
        }

        function f() {
            F.hide()
        }

        function g() {
            var e = "top" === CP.ui.layout ? m : v;
            e.apply(this, arguments)
        }

        function m() {
            J = U.height(), z = H.height(), W = O.outerHeight(), oe = parseInt(U.css("minHeight"), 10), re && C()
        }

        function v() {
            K = U.width(), Y = H.width(), G = O.outerWidth()
        }

        function C() {
            q = te = j.height(), Q = I.height(), ee = J = U.height(), X = Q, Z || (Z = I.find(".editor-resizer").outerHeight() + I.find(".powers").outerHeight())
        }

        function b() {
            var e = "top" === CP.ui.layout ? y : w;
            e.apply(this, arguments)
        }

        function y(e, t, n) {
            var s = J + n.y;
            s = Math.max(0, s);
            var i = z - W;
            i -= re ? Z : 0, s = Math.min(i, s), s = Math.max(oe, s), U.height(s), q = z - s - W, P()
        }

        function P() {
            if (re) {
                var e = (Q - Z) / (q - Z);
                e = Math.min(1, e), Q = Math.min(q, Q), c(e)
            }
        }

        function w(e, t, n) {
            var s = n.x * ("right" === CP.ui.layout ? -1 : 1),
                i = K + s;
            i = Math.max(0, i);
            var o = Y - G;
            i = Math.min(o, i), U.width(i), M()
        }

        function S() {
            "function" == typeof Comment.tweakCommentsSize && Comment.tweakCommentsSize(), Hub.pub("editor-refresh", {
                delay: 0
            });
            var e = 0;
            e = "top" === CP.ui.layout ? U.height() / (N.height() - W) : U.width() / window.innerWidth;
            var t = {
                editor: e
            };
            re && (t.console = ce), Hub.pub("editor-sizes-change", t)
        }

        function T() {
            CP.EditorLayout._canDrive && C()
        }

        function E(e, t, n) {
            if (CP.EditorLayout._canDrive) {
                var s = X - n.y;
                s = Math.max(Z, s), s = Math.min(q, s);
                var i = q - Z,
                    o = (s - Z) / i;
                c(o), x(n)
            }
        }

        function x(e) {
            if ("top" === CP.ui.layout) {
                var t = te + e.y - Q;
                t >= 0 || (ee = Math.min(J + t, ee), U.height(ee), q = Math.max(q, te - t))
            }
        }

        function A() {
            CP.EditorLayout._canDrive && Hub.pub("editor-sizes-change", {
                console: ce
            })
        }

        function k() {
            if (CP.EditorLayout._canDrive) {
                var e = 1 === ce ? ae : 1;
                d(e)
            }
        }

        function M() {
            clearTimeout(ie), L.addClass("visible"), L.text(R.width() + "px"), ie = setTimeout(function() {
                L.removeClass("visible")
            }, 1e3)
        }

        function D() {
            if ("top" === CP.ui.layout) {
                var e = !!navigator.userAgent.match(/(iPad)/g);
                e && $("head").append("<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no'>")
            }
        }
        CP.EditorLayout = {};
        var R, O, F, L, H, U, I, j, B, V, N, z, W, J, Y, G, K, q, X, Q, Z, ee, te, ne = $(window),
            se = $("body"),
            ie = 0,
            oe = 0,
            re = !1,
            ae = 1 / 3,
            ce = ae;
        CP.EditorLayout.init = function() {
            e(), t(), r(), this.bindToEnableDisableHubEvents(), h(), D()
        }, CP.EditorLayout.setConsoleSize = c, CP.EditorLayout.openConsole = function() {
            d(ce)
        }, CP.EditorLayout.closeConsole = function() {
            d("closed")
        }, CP.EditorLayout.toggleConsole = function() {
            "closed" === CP.ui.editorSizes.console ? (Hub.pub("ui-console-opened"), this.openConsole()) : this.closeConsole()
        }, CP.EditorLayout.doneLoading = function() {
            se.removeClass("prelayout"), ne.load(function() {
                se.removeClass("preload")
            })
        }, _.extend(CP.EditorLayout, EnableDisableDriver), CP.EditorLayout._getAllUIElements = function() {
            return [B, V]
        }
    }();
var ViewSwitcher = {
    TYPES: ["top", "left", "right"],
    $body: $("body"),
    $viewSwitcher: $(".view-switcher"),
    $mainHeader: $("#main-header"),
    init: function() {
        this._bindToDOM(), this._bindToHub()
    },
    _bindToDOM: function() {
        this.$viewSwitcher.length && (this.$viewSwitcher.find(".pres-link:not('.upgrade-upsell')")._on("click", this.slideHeaderAway, this, !0), this.$viewSwitcher.find(".learn-more")._on("click", this.openLearnMoreLink, this)), $("[data-layout-type]")._on("click", this.onLayoutTypeButtonClick, this)
    },
    slideHeaderAway: function(e) {
        e.metaKey || (this.$viewSwitcher.removeClass("open"), this.$mainHeader.addClass("up-and-away"))
    },
    openLearnMoreLink: function(e, t) {
        window.open(t.data("href"), "_blank")
    },
    onLayoutTypeButtonClick: function(e) {
        var t = $(e.currentTarget),
            n = t.attr("data-layout-type");
        this.changeLayout(n), ga("set", "dimension3", n), ga("send", "event", {
            eventCategory: "Layout Type",
            eventAction: "Change",
            eventLabel: n
        })
    },
    _bindToHub: function() {
        Hub.sub("server-ui-change", $.proxy(this._onServerUIChange, this))
    },
    _onServerUIChange: function(e, t) {
        t.ui && t.ui.layout && this.changeUILayout(t.ui.layout)
    },
    changeLayout: function(e) {
        this.changeUILayout(e), $.cookie("__cp_layout", e, {
            expires: 30,
            path: "/"
        })
    },
    changeUILayout: function(e) {
        this.getIsValidLayoutType(e), this.$body.removeClass("layout-" + CP.ui.layout);
        var t = "left" === e || "right" === e;
        this.$body[t ? "addClass" : "removeClass"]("layout-side"), this.$body.addClass("layout-" + e), CP.ui.layout = e, Hub.pub("ui-change", {
            ui: {
                layout: e
            }
        }), Hub.pub("editor-refresh", {
            delay: 0
        })
    },
    getIsValidLayoutType: function(e) {
        var t = _.contains(this.TYPES, e);
        if (!t) throw "Invalid layout type: " + e;
        return t
    }
};
ViewSwitcher.init();
var Follow = {
        init: function() {
            _.extend(this, AJAXUtil), this.profiled = __profiled, this._bindToDOM()
        },
        _bindToDOM: function() {
            this._addOnHoverChangeToFollowButton(), $("#follow-this-user, #following-this-user")._on("click", this.followThisUser, this)
        },
        _addOnHoverChangeToFollowButton: function() {
            $("#following-this-user").hover(function() {
                $(this).addClass("red").data("text-value", $(this).html()).html("<svg class='icon-x'><use xlink:href='#x'></use></svg> Following")
            }, function() {
                $(this).removeClass("red").html($(this).data("text-value"))
            })
        },
        followThisUser: function() {
            var e = this._getActionTypeTaken();
            this._updateFollowButtonImmediately(e), this.post(this._getFollowUnfollowURL(e), {}, this._doneFollowThisUser)
        },
        _getActionTypeTaken: function() {
            return $("#follow-this-user").is(":visible") ? "follow" : "unfollow"
        },
        _getFollowUnfollowURL: function(e) {
            var t = "/follow/<%= type %>/<%= username %>/<%= action %>";
            return _.template(t, {
                type: this.profiled.type,
                username: this.profiled.username,
                action: e
            })
        },
        _updateFollowButtonImmediately: function(e) {
            "follow" === e ? ($("#follow-this-user").hide(), $("#following-this-user").show()) : ($("#follow-this-user").show(), $("#following-this-user").hide())
        },
        _doneFollowThisUser: function(e) {
            $("#followers-tab").hasClass("active") && $("#followers").replaceWith(e.html)
        }
    },
    Drawer = {
        drawer: $("#drawer"),
        drawerOpen: !1,
        drawerHasBeenOpened: !1,
        popupName: "drawer",
        init: function() {
            this.bindUIEvents(), this.bindToHub()
        },
        bindUIEvents: function() {
            $("#view-details-button")._on("click", this.toggleDrawer, this), $("#drawer").on("click", "#drawer-close-button", this.closeDrawer), Keytrap.bind("esc", function() {
                Drawer.closeDrawer()
            })
        },
        bindToHub: function() {
            Hub.sub("key", $.proxy(this.onKey, this)), Hub.sub("popup-open", $.proxy(this.onPopupOpen, this))
        },
        toggleDrawer: function() {
            Drawer.drawerOpen ? Drawer.closeDrawer() : Drawer.drawerHasBeenOpened ? Drawer.openDrawer() : Drawer.setUpDrawer()
        },
        openDrawer: function() {
            Drawer.drawer.addClass("open"), Drawer.drawerOpen = !0, Drawer.drawerHasBeenOpened = !0, Hub.pub("popup-open", this.popupName)
        },
        closeDrawer: function() {
            Drawer.drawer.removeClass("open"), Drawer.drawerOpen = !1
        },
        setUpDrawer: function() {
            Drawer.drawerHasBeenOpened = !0;
            var e = window.location.href,
                t = e.replace("/pen/", "/drawer/");
            $.get(t, function(e) {
                Drawer.drawerContent = e
            }).then(function() {
                Drawer.drawer.append(Drawer.drawerContent);
                var e = $("#drawer-css-url").val(),
                    t = $("#drawer-js-url").val();
                $.when($.ajax({
                    url: e,
                    cache: !1,
                    success: function(e) {
                        $("<style></style>").appendTo("head").html(e)
                    }
                }), $.getScript(t)).then(function() {
                    Comment.init(), Follow.init(), Drawer.openDrawer()
                })
            })
        },
        onKey: function(e, t) {
            "esc" === t.key && this.closeDrawer()
        },
        onPopupOpen: function(e, t) {
            t !== this.popupName && this.closeDrawer()
        }
    };
Drawer.init(),
    function() {
        function e() {
            d.on("click", t), $("#popup-overlay").on("click", r)
        }

        function t(e) {
            e.preventDefault(), p ? (n(), o()) : s()
        }

        function n() {
            Hub.pub("embed-reshown", {})
        }

        function s() {
            if (!f) {
                var e = _.template("<%= username %>/embed/mini/builder/<%= slugHash %>/", {
                    username: CP.profiled.base_url,
                    slugHash: CP.pen.getActiveSlugHash()
                });
                $.when(AJAXUtil.get(e, {}, $.noop)).done(i).fail(AJAXUtil.showStandardErrorMessage).always(function() {
                    f = !1
                }), f = !0
            }
        }

        function i(e) {
            u = $("<div />", {
                id: "embed-modal",
                "class": "modal embed-modal loading",
                html: e.html
            }), u.appendTo(l), $.getScript(__embed_modal_script), $("#embed-modal-close-button").on("click", r), Keytrap.bind("esc", r), p = !0, o()
        }

        function o() {
            h || (u.addClass("open"), CP.showPopupOverlay(), h = !0, Hub.pub("popup-open", g))
        }

        function r() {
            h && (u && u.removeClass("open"), embedCommonUI.removeCustomCSS(), CP.hidePopupOverlay(), h = !1)
        }

        function a() {
            Hub.sub("popup-open", c)
        }

        function c(e, t) {
            t !== g && r()
        }
        CP.EmbedModal = {};
        var u, l = $("body"),
            d = $(".embed-builder-button"),
            h = !1,
            p = !1,
            f = !1,
            g = "embedModal";
        CP.EmbedModal.init = function() {
            e(), a()
        }, CP.EmbedModal.init()
    }();
var LiveRoom = Class.extend({
    init: function(e, t) {
        t.connectToFirebase && (this.rtData = e, this._listenToMessages(), Hub.sub("live_change", $.proxy(this._publishLiveUpdate, this)))
    },
    _listenToMessages: function() {},
    getPenRef: function() {
        return CPFirebase.getRef("pen")
    },
    _publishLiveUpdate: function(e, t) {
        CPFirebase.safelySetRef(this.getPenRef(), t)
    }
});
! function(e) {
    if ("object" == typeof exports && "undefined" != typeof module) module.exports = e();
    else if ("function" == typeof define && define.amd) define([], e);
    else {
        var t;
        t = "undefined" != typeof window ? window : "undefined" != typeof global ? global : "undefined" != typeof self ? self : this, t.Clipboard = e()
    }
}(function() {
    var e;
    return function e(t, n, s) {
        function i(r, a) {
            if (!n[r]) {
                if (!t[r]) {
                    var c = "function" == typeof require && require;
                    if (!a && c) return c(r, !0);
                    if (o) return o(r, !0);
                    var u = new Error("Cannot find module '" + r + "'");
                    throw u.code = "MODULE_NOT_FOUND", u
                }
                var l = n[r] = {
                    exports: {}
                };
                t[r][0].call(l.exports, function(e) {
                    var n = t[r][1][e];
                    return i(n ? n : e)
                }, l, l.exports, e, t, n, s)
            }
            return n[r].exports
        }
        for (var o = "function" == typeof require && require, r = 0; r < s.length; r++) i(s[r]);
        return i
    }({
        1: [function(e, t) {
            var n = e("matches-selector");
            t.exports = function(e, t, s) {
                for (var i = s ? e : e.parentNode; i && i !== document;) {
                    if (n(i, t)) return i;
                    i = i.parentNode
                }
            }
        }, {
            "matches-selector": 5
        }],
        2: [function(e, t) {
            function n(e, t, n, i, o) {
                var r = s.apply(this, arguments);
                return e.addEventListener(n, r, o), {
                    destroy: function() {
                        e.removeEventListener(n, r, o)
                    }
                }
            }

            function s(e, t, n, s) {
                return function(n) {
                    n.delegateTarget = i(n.target, t, !0), n.delegateTarget && s.call(e, n)
                }
            }
            var i = e("closest");
            t.exports = n
        }, {
            closest: 1
        }],
        3: [function(e, t, n) {
            n.node = function(e) {
                return void 0 !== e && e instanceof HTMLElement && 1 === e.nodeType
            }, n.nodeList = function(e) {
                var t = Object.prototype.toString.call(e);
                return void 0 !== e && ("[object NodeList]" === t || "[object HTMLCollection]" === t) && "length" in e && (0 === e.length || n.node(e[0]))
            }, n.string = function(e) {
                return "string" == typeof e || e instanceof String
            }, n.fn = function(e) {
                var t = Object.prototype.toString.call(e);
                return "[object Function]" === t
            }
        }, {}],
        4: [function(e, t) {
            function n(e, t, n) {
                if (!e && !t && !n) throw new Error("Missing required arguments");
                if (!r.string(t)) throw new TypeError("Second argument must be a String");
                if (!r.fn(n)) throw new TypeError("Third argument must be a Function");
                if (r.node(e)) return s(e, t, n);
                if (r.nodeList(e)) return i(e, t, n);
                if (r.string(e)) return o(e, t, n);
                throw new TypeError("First argument must be a String, HTMLElement, HTMLCollection, or NodeList")
            }

            function s(e, t, n) {
                return e.addEventListener(t, n), {
                    destroy: function() {
                        e.removeEventListener(t, n)
                    }
                }
            }

            function i(e, t, n) {
                return Array.prototype.forEach.call(e, function(e) {
                    e.addEventListener(t, n)
                }), {
                    destroy: function() {
                        Array.prototype.forEach.call(e, function(e) {
                            e.removeEventListener(t, n)
                        })
                    }
                }
            }

            function o(e, t, n) {
                return a(document.body, e, t, n)
            }
            var r = e("./is"),
                a = e("delegate");
            t.exports = n
        }, {
            "./is": 3,
            delegate: 2
        }],
        5: [function(e, t) {
            function n(e, t) {
                if (i) return i.call(e, t);
                for (var n = e.parentNode.querySelectorAll(t), s = 0; s < n.length; ++s)
                    if (n[s] == e) return !0;
                return !1
            }
            var s = Element.prototype,
                i = s.matchesSelector || s.webkitMatchesSelector || s.mozMatchesSelector || s.msMatchesSelector || s.oMatchesSelector;
            t.exports = n
        }, {}],
        6: [function(e, t) {
            function n(e) {
                var t;
                if ("INPUT" === e.nodeName || "TEXTAREA" === e.nodeName) e.focus(), e.setSelectionRange(0, e.value.length), t = e.value;
                else {
                    e.hasAttribute("contenteditable") && e.focus();
                    var n = window.getSelection(),
                        s = document.createRange();
                    s.selectNodeContents(e), n.removeAllRanges(), n.addRange(s), t = n.toString()
                }
                return t
            }
            t.exports = n
        }, {}],
        7: [function(e, t) {
            function n() {}
            n.prototype = {
                on: function(e, t, n) {
                    var s = this.e || (this.e = {});
                    return (s[e] || (s[e] = [])).push({
                        fn: t,
                        ctx: n
                    }), this
                },
                once: function(e, t, n) {
                    function s() {
                        i.off(e, s), t.apply(n, arguments)
                    }
                    var i = this;
                    return s._ = t, this.on(e, s, n)
                },
                emit: function(e) {
                    var t = [].slice.call(arguments, 1),
                        n = ((this.e || (this.e = {}))[e] || []).slice(),
                        s = 0,
                        i = n.length;
                    for (s; s < i; s++) n[s].fn.apply(n[s].ctx, t);
                    return this
                },
                off: function(e, t) {
                    var n = this.e || (this.e = {}),
                        s = n[e],
                        i = [];
                    if (s && t)
                        for (var o = 0, r = s.length; o < r; o++) s[o].fn !== t && s[o].fn._ !== t && i.push(s[o]);
                    return i.length ? n[e] = i : delete n[e], this
                }
            }, t.exports = n
        }, {}],
        8: [function(t, n, s) {
            ! function(i, o) {
                if ("function" == typeof e && e.amd) e(["module", "select"], o);
                else if ("undefined" != typeof s) o(n, t("select"));
                else {
                    var r = {
                        exports: {}
                    };
                    o(r, i.select), i.clipboardAction = r.exports
                }
            }(this, function(e, t) {
                "use strict";

                function n(e) {
                    return e && e.__esModule ? e : {
                        "default": e
                    }
                }

                function s(e, t) {
                    if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function")
                }
                var i = n(t),
                    o = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function(e) {
                        return typeof e
                    } : function(e) {
                        return e && "function" == typeof Symbol && e.constructor === Symbol ? "symbol" : typeof e
                    },
                    r = function() {
                        function e(e, t) {
                            for (var n = 0; n < t.length; n++) {
                                var s = t[n];
                                s.enumerable = s.enumerable || !1, s.configurable = !0, "value" in s && (s.writable = !0), Object.defineProperty(e, s.key, s)
                            }
                        }
                        return function(t, n, s) {
                            return n && e(t.prototype, n), s && e(t, s), t
                        }
                    }(),
                    a = function() {
                        function e(t) {
                            s(this, e), this.resolveOptions(t), this.initSelection()
                        }
                        return e.prototype.resolveOptions = function() {
                            var e = arguments.length <= 0 || void 0 === arguments[0] ? {} : arguments[0];
                            this.action = e.action, this.emitter = e.emitter, this.target = e.target, this.text = e.text, this.trigger = e.trigger, this.selectedText = ""
                        }, e.prototype.initSelection = function() {
                            this.text ? this.selectFake() : this.target && this.selectTarget()
                        }, e.prototype.selectFake = function() {
                            var e = this,
                                t = "rtl" == document.documentElement.getAttribute("dir");
                            this.removeFake(), this.fakeHandlerCallback = function() {
                                return e.removeFake()
                            }, this.fakeHandler = document.body.addEventListener("click", this.fakeHandlerCallback) || !0, this.fakeElem = document.createElement("textarea"), this.fakeElem.style.fontSize = "12pt", this.fakeElem.style.border = "0", this.fakeElem.style.padding = "0", this.fakeElem.style.margin = "0", this.fakeElem.style.position = "absolute", this.fakeElem.style[t ? "right" : "left"] = "-9999px", this.fakeElem.style.top = (window.pageYOffset || document.documentElement.scrollTop) + "px", this.fakeElem.setAttribute("readonly", ""), this.fakeElem.value = this.text, document.body.appendChild(this.fakeElem), this.selectedText = (0, i["default"])(this.fakeElem), this.copyText()
                        }, e.prototype.removeFake = function() {
                            this.fakeHandler && (document.body.removeEventListener("click", this.fakeHandlerCallback), this.fakeHandler = null, this.fakeHandlerCallback = null), this.fakeElem && (document.body.removeChild(this.fakeElem), this.fakeElem = null)
                        }, e.prototype.selectTarget = function() {
                            this.selectedText = (0, i["default"])(this.target), this.copyText()
                        }, e.prototype.copyText = function() {
                            var e = void 0;
                            try {
                                e = document.execCommand(this.action)
                            } catch (t) {
                                e = !1
                            }
                            this.handleResult(e)
                        }, e.prototype.handleResult = function(e) {
                            e ? this.emitter.emit("success", {
                                action: this.action,
                                text: this.selectedText,
                                trigger: this.trigger,
                                clearSelection: this.clearSelection.bind(this)
                            }) : this.emitter.emit("error", {
                                action: this.action,
                                trigger: this.trigger,
                                clearSelection: this.clearSelection.bind(this)
                            })
                        }, e.prototype.clearSelection = function() {
                            this.target && this.target.blur(), window.getSelection().removeAllRanges()
                        }, e.prototype.destroy = function() {
                            this.removeFake()
                        }, r(e, [{
                            key: "action",
                            set: function() {
                                var e = arguments.length <= 0 || void 0 === arguments[0] ? "copy" : arguments[0];
                                if (this._action = e, "copy" !== this._action && "cut" !== this._action) throw new Error('Invalid "action" value, use either "copy" or "cut"')
                            },
                            get: function() {
                                return this._action
                            }
                        }, {
                            key: "target",
                            set: function(e) {
                                if (void 0 !== e) {
                                    if (!e || "object" !== ("undefined" == typeof e ? "undefined" : o(e)) || 1 !== e.nodeType) throw new Error('Invalid "target" value, use a valid Element');
                                    if ("copy" === this.action && e.hasAttribute("disabled")) throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');
                                    if ("cut" === this.action && (e.hasAttribute("readonly") || e.hasAttribute("disabled"))) throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');
                                    this._target = e
                                }
                            },
                            get: function() {
                                return this._target
                            }
                        }]), e
                    }();
                e.exports = a
            })
        }, {
            select: 6
        }],
        9: [function(t, n, s) {
            ! function(i, o) {
                if ("function" == typeof e && e.amd) e(["module", "./clipboard-action", "tiny-emitter", "good-listener"], o);
                else if ("undefined" != typeof s) o(n, t("./clipboard-action"), t("tiny-emitter"), t("good-listener"));
                else {
                    var r = {
                        exports: {}
                    };
                    o(r, i.clipboardAction, i.tinyEmitter, i.goodListener), i.clipboard = r.exports
                }
            }(this, function(e, t, n, s) {
                "use strict";

                function i(e) {
                    return e && e.__esModule ? e : {
                        "default": e
                    }
                }

                function o(e, t) {
                    if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function")
                }

                function r(e, t) {
                    if (!e) throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
                    return !t || "object" != typeof t && "function" != typeof t ? e : t
                }

                function a(e, t) {
                    if ("function" != typeof t && null !== t) throw new TypeError("Super expression must either be null or a function, not " + typeof t);
                    e.prototype = Object.create(t && t.prototype, {
                        constructor: {
                            value: e,
                            enumerable: !1,
                            writable: !0,
                            configurable: !0
                        }
                    }), t && (Object.setPrototypeOf ? Object.setPrototypeOf(e, t) : e.__proto__ = t)
                }

                function c(e, t) {
                    var n = "data-clipboard-" + e;
                    if (t.hasAttribute(n)) return t.getAttribute(n)
                }
                var u = i(t),
                    l = i(n),
                    d = i(s),
                    h = function(e) {
                        function t(n, s) {
                            o(this, t);
                            var i = r(this, e.call(this));
                            return i.resolveOptions(s), i.listenClick(n), i
                        }
                        return a(t, e), t.prototype.resolveOptions = function() {
                            var e = arguments.length <= 0 || void 0 === arguments[0] ? {} : arguments[0];
                            this.action = "function" == typeof e.action ? e.action : this.defaultAction, this.target = "function" == typeof e.target ? e.target : this.defaultTarget, this.text = "function" == typeof e.text ? e.text : this.defaultText
                        }, t.prototype.listenClick = function(e) {
                            var t = this;
                            this.listener = (0, d["default"])(e, "click", function(e) {
                                return t.onClick(e)
                            })
                        }, t.prototype.onClick = function(e) {
                            var t = e.delegateTarget || e.currentTarget;
                            this.clipboardAction && (this.clipboardAction = null), this.clipboardAction = new u["default"]({
                                action: this.action(t),
                                target: this.target(t),
                                text: this.text(t),
                                trigger: t,
                                emitter: this
                            })
                        }, t.prototype.defaultAction = function(e) {
                            return c("action", e)
                        }, t.prototype.defaultTarget = function(e) {
                            var t = c("target", e);
                            if (t) return document.querySelector(t)
                        }, t.prototype.defaultText = function(e) {
                            return c("text", e)
                        }, t.prototype.destroy = function() {
                            this.listener.destroy(), this.clipboardAction && (this.clipboardAction.destroy(), this.clipboardAction = null)
                        }, t
                    }(l["default"]);
                e.exports = h
            })
        }, {
            "./clipboard-action": 8,
            "good-listener": 4,
            "tiny-emitter": 7
        }]
    }, {}, [9])(9)
}), $.event.props.push("dataTransfer"), window.Assets = {
    s: {
        assetsArea: $("#assets-area"),
        manualInput: $("#manual-file-upload"),
        dragPrevent: !1,
        files: []
    },
    _bindToDragAndDrop: function() {
        this.s.assetsArea = $("#assets-area"), this.s.manualInput = $("#manual-file-upload");
        var e, t = this;
        this.s.assetsArea._on("dragenter dragover", function(n) {
            clearTimeout(e), !t.s.dragPrevent && t._testIfContainsFiles(n) && t.makeLookDroppable()
        }), this.s.assetsArea._on("dragleave dragout", function() {
            e = setTimeout(function() {
                t.unmakeLookDroppable()
            }, 200)
        }), this.s.assetsArea._on("drop", function(e) {
            e.preventDefault(), t._testIfContainsFiles(e) && t.handleFileDrop(e)
        }), this.s.manualInput._on("change", this.handleFileSelect, this)
    },
    _testIfContainsFiles: function(e) {
        return !!e.dataTransfer.types && _.contains(e.dataTransfer.types, "Files")
    },
    makeLookDroppable: function() {
        this.s.assetsArea.addClass("draggedOn")
    },
    unmakeLookDroppable: function() {
        this.s.assetsArea.removeClass("draggedOn")
    },
    handleFileDrop: function(e) {
        this.processFile(e.dataTransfer.files), this.unmakeLookDroppable()
    },
    handleFileSelect: function(e) {
        this.processFile(e.target.files)
    },
    MAX_FILES: 10,
    MAX_FILE_SIZE: 2097152,
    processFile: function(e) {
        this.numFiles = e.length, this.errors = [], e.length > this.MAX_FILES ? this._showTooManyFilesMessage() : this._filesTooBig(e) ? this._showFileTooBigMessage() : this._filesHaveBadFileExtension(e) ? this._showBadFileExtensionMessage() : AssetsData.anyDuplicates(e) ? this._askUserIfTheyWantToOverwriteFiles(e) : this._signAssetsAndUpload(e, "noduplicates")
    },
    _askUserIfTheyWantToOverwriteFiles: function(e) {
        var t = this;
        $.showModal("/ajax/assets/overwrite_or_update_assets", "modal-error", function() {
            t._updateFilesToBeOverwrittenModal(e), $("#confirm-overwrite")._on("click", function() {
                $.hideMessage(), t._signAssetsAndUpload(e, "ovewrite")
            }), $("#confirm-create-new")._on("click", function() {
                $.hideMessage(), t._signAssetsAndUpload(e, "makeunique")
            })
        })
    },
    _updateFilesToBeOverwrittenModal: function(e) {
        var t = AssetsData.duplicateNames(e),
            n = this._filesToBeOverwrittenHTML(t);
        $("#list-of-files").html(n)
    },
    _filesToBeOverwrittenHTML: function(e) {
        var t = "";
        return _.forEach(e, function(e) {
            t += "<p>" + e + "</p>"
        }), t
    },
    _signAssetsAndUpload: function(e, t) {
        $.showMessage("Uploading file...");
        for (var n = 0; n < e.length; n++) {
            var s = e[n],
                i = FileUtil.makeNameSafeToSave(s.name);
            this.s.files[i] = s;
            var o = 0,
                r = s.name,
                a = AssetsData.findAssetByName(s.name);
            a && ("ovewrite" === t ? o = a.id : "makeunique" === t && (r = AssetsData.buildUniqueName(a.name)));
            var c = this;
            S3.uploadAccountAssetFileToS3(s, r, function(e) {
                e.success ? c._doneUploadingAccountAssetToS3(e, o) : c._assetUploadFailed(e)
            })
        }
    },
    _doneUploadingAccountAssetToS3: function(e, t) {
        var n = t > 0 ? "put" : "post",
            s = t > 0 ? "/uploaded_assets/account_asset/" + t : "/uploaded_assets/account_asset";
        AJAXUtil[n](s, this._getAccountAssetPostParams(e.file, e.fileName), $.proxy(this._uploadComplete, this), $.proxy(this._assetUploadFailed, this))
    },
    _getAccountAssetPostParams: function(e, t) {
        return {
            name: t,
            content_type: e.type,
            size_in_bytes: e.size,
            response_type: window.__pageType
        }
    },
    _filesTooBig: function(e) {
        var t = this;
        return _.some(e, function(e) {
            return e.size > t.MAX_FILE_SIZE
        })
    },
    _filesHaveBadFileExtension: function(e) {
        return _.some(e, function(e) {
            return !!e.name.match(/^.+\.(exe)$/) || !e.name.match(/^.+\..+$/)
        })
    },
    _showTooManyFilesMessage: function() {
        $.showModal("/ajax/assets/too_many_files", "modal-error")
    },
    _showFileTooBigMessage: function() {
        $.showModal("/ajax/assets/file_too_big", "modal-error")
    },
    _showBadFileExtensionMessage: function() {
        $.showModal("/ajax/assets/bad_extension", "modal-error")
    },
    _assetUploadFailed: function(e) {
        this._decrementFileCount(), 0 === this.numFiles && AJAXUtil.showStandardErrorMessage(e)
    },
    _decrementFileCount: function() {
        --this.numFiles
    }
}, "undefined" == typeof window.Copy && (window.Copy = {}), _.extend(window.Copy, {
    assetRenamed: 'Asset "<%= name %>" renamed successfully!',
    assetUploaded: 'Asset "<%= name %>" uploaded successfully!'
}), window.Asset = Class.extend({
    init: function(e) {
        _.extend(this, AJAXUtil), _.extend(this, e)
    },
    save: function(e) {
        this.put("/uploaded_assets/account_asset/" + this.id, {
            text: e,
            update_type: "text",
            response_type: window.__pageType
        }, this._doneSave)
    },
    _doneSave: function(e) {
        Hub.pub("asset-updated", e)
    },
    "delete": function(e) {
        var t = "/uploaded_assets/account_asset/" + this.id,
            n = this;
        this.del(t, {
            response_type: window.__pageType
        }, function(t) {
            n._doneDelete(t, e)
        })
    },
    _doneDelete: function(e, t) {
        Hub.pub("asset-deleted", e), "function" == typeof t && t(e)
    }
});
var AssetsData = {
        assets: {},
        selectedAsset: null,
        boundToHub: !1,
        init: function() {
            this._bindToHub(), this._buildAssetObjectsFromDBAssets()
        },
        _bindToHub: function() {
            this.boundToHub || (this.boundToHub = !0, Hub.sub("asset-added", $.proxy(this._onAssetAdded, this)), Hub.sub("asset-deleted", $.proxy(this._onAssetDeleted, this)), Hub.sub("asset-selected", $.proxy(this._onAssetSelected, this)), Hub.sub("asset-deselected", $.proxy(this._onAssetDeselected, this)))
        },
        _onAssetAdded: function(e, t) {
            this.add(t.asset)
        },
        _onAssetSelected: function(e, t) {
            this.selectedAsset = this.find(t.id)
        },
        _onAssetDeleted: function(e, t) {
            this.del(t.asset.id), this.selectedAsset = null
        },
        _onAssetDeselected: function() {
            this.selectedAsset = null
        },
        getSelectedAsset: function() {
            return this.selectedAsset
        },
        _buildAssetObjectsFromDBAssets: function() {
            var e = this;
            _.forEach(__assets, function(t) {
                e.assets[t.id] = new Asset(t)
            })
        },
        find: function(e) {
            return this.assets[e]
        },
        add: function(e) {
            __assets[e.id] = e, this.assets[e.id] = new Asset(e)
        },
        del: function(e) {
            delete __assets[e], delete this.assets[e]
        },
        anyDuplicates: function(e) {
            return this.duplicateNames(e).length > 0
        },
        duplicateNames: function(e) {
            var t = this._safeNames(this.assets),
                n = this._safeNames(e);
            return _.intersection(t, n)
        },
        _safeNames: function(e) {
            return _.map(e, function(e) {
                return FileUtil.makeNameSafeToSave(e.name)
            })
        },
        findAssetByName: function(e) {
            return _.find(this.assets, function(t) {
                return t.name === e
            })
        },
        buildUniqueName: function(e) {
            for (var t = e, n = this.findAssetByName(t); n;) t = this._addUnderscoreOneToExistingName(t), n = this.findAssetByName(t);
            return t
        },
        _addUnderscoreOneToExistingName: function(e) {
            var t = e.split("."),
                n = t.pop();
            return t.join(".") + "_copy." + n
        }
    },
    FileUtil = {
        makeNameSafeToSave: function(e) {
            return e.split(" ").join("_")
        }
    };
! function() {
    function e() {
        var e = '<ul class="design-asset-color-swatches">';
        for (var n in t) e += "<li><h2>" + n + "</h2>", t[n].forEach(function(t) {
            e += '<span class="design-asset-copy-as" data-clipboard-text="' + t + '" style="background-color: ' + t + ';"></span>'
        }), e += "</li>";
        e += "</ul>", document.querySelector("#color-picker-area").innerHTML = e
    }
    var t = {
        Red: ["#FFEBEE", "#FFCDD2", "#EF9A9A", "#E57373", "#EF5350", "#F44336", "#E53935", "#D32F2F", "#C62828", "#B71C1C", "#FF8A80", "#FF5252", "#FF1744", "#D50000"],
        Pink: ["#FCE4EC", "#F8BBD0", "#F48FB1", "#F06292", "#EC407A", "#E91E63", "#D81B60", "#C2185B", "#AD1457", "#880E4F", "#FF80AB", "#FF4081", "#F50057", "#C51162"],
        Purple: ["#F3E5F5", "#E1BEE7", "#CE93D8", "#BA68C8", "#AB47BC", "#9C27B0", "#8E24AA", "#7B1FA2", "#6A1B9A", "#4A148C", "#EA80FC", "#E040FB", "#D500F9", "#AA00FF"],
        "Deep Purple": ["#EDE7F6", "#D1C4E9", "#B39DDB", "#9575CD", "#7E57C2", "#673AB7", "#5E35B1", "#512DA8", "#4527A0", "#311B92", "#B388FF", "#7C4DFF", "#651FFF", "#6200EA"],
        Indigo: ["#E8EAF6", "#C5CAE9", "#9FA8DA", "#7986CB", "#5C6BC0", "#3F51B5", "#3949AB", "#303F9F", "#283593", "#1A237E", "#8C9EFF", "#536DFE", "#3D5AFE", "#304FFE"],
        Blue: ["#E3F2FD", "#BBDEFB", "#90CAF9", "#64B5F6", "#42A5F5", "#2196F3", "#1E88E5", "#1976D2", "#1565C0", "#0D47A1", "#82B1FF", "#448AFF", "#2979FF", "#2962FF"],
        "Light Blue": ["#E1F5FE", "#B3E5FC", "#81D4FA", "#4FC3F7", "#29B6F6", "#03A9F4", "#039BE5", "#0288D1", "#0277BD", "#01579B", "#80D8FF", "#40C4FF", "#00B0FF", "#0091EA"],
        Cyan: ["#E0F7FA", "#B2EBF2", "#80DEEA", "#4DD0E1", "#26C6DA", "#00BCD4", "#00ACC1", "#0097A7", "#00838F", "#006064", "#84FFFF", "#18FFFF", "#00E5FF", "#00B8D4"],
        Teal: ["#E0F2F1", "#B2DFDB", "#80CBC4", "#4DB6AC", "#26A69A", "#009688", "#00897B", "#00796B", "#00695C", "#004D40", "#A7FFEB", "#64FFDA", "#1DE9B6", "#00BFA5"],
        Green: ["#E8F5E9", "#C8E6C9", "#A5D6A7", "#81C784", "#66BB6A", "#4CAF50", "#43A047", "#388E3C", "#2E7D32", "#1B5E20", "#B9F6CA", "#69F0AE", "#00E676", "#00C853"],
        "Light Green": ["#F1F8E9", "#DCEDC8", "#C5E1A5", "#AED581", "#9CCC65", "#8BC34A", "#7CB342", "#689F38", "#558B2F", "#33691E", "#CCFF90", "#B2FF59", "#76FF03", "#64DD17"],
        Lime: ["#F9FBE7", "#F0F4C3", "#E6EE9C", "#DCE775", "#D4E157", "#CDDC39", "#C0CA33", "#AFB42B", "#9E9D24", "#827717", "#F4FF81", "#EEFF41", "#C6FF00", "#AEEA00"],
        Yellow: ["#FFFDE7", "#FFF9C4", "#FFF59D", "#FFF176", "#FFEE58", "#FFEB3B", "#FDD835", "#FBC02D", "#F9A825", "#F57F17", "#FFFF8D", "#FFFF00", "#FFEA00", "#FFD600"],
        Amber: ["#FFF8E1", "#FFECB3", "#FFE082", "#FFD54F", "#FFCA28", "#FFC107", "#FFB300", "#FFA000", "#FF8F00", "#FF6F00", "#FFE57F", "#FFD740", "#FFC400", "#FFAB00"],
        Orange: ["#FFF3E0", "#FFE0B2", "#FFCC80", "#FFB74D", "#FFA726", "#FF9800", "#FB8C00", "#F57C00", "#EF6C00", "#E65100", "#FFD180", "#FFAB40", "#FF9100", "#FF6D00"],
        "Deep Orange": ["#FBE9E7", "#FFCCBC", "#FFAB91", "#FF8A65", "#FF7043", "#FF5722", "#F4511E", "#E64A19", "#D84315", "#BF360C", "#FF9E80", "#FF6E40", "#FF3D00", "#DD2C00"],
        Brown: ["#EFEBE9", "#D7CCC8", "#BCAAA4", "#A1887F", "#8D6E63", "#795548", "#6D4C41", "#5D4037", "#4E342E", "#3E2723"],
        Grey: ["#FAFAFA", "#F5F5F5", "#EEEEEE", "#E0E0E0", "#BDBDBD", "#9E9E9E", "#757575", "#616161", "#424242", "#212121"],
        "Blue Grey": ["#ECEFF1", "#CFD8DC", "#B0BEC5", "#90A4AE", "#78909C", "#607D8B", "#546E7A", "#455A64", "#37474F", "#263238"]
    };
    CP.ColorPicker = {}, CP.ColorPicker.init = function() {
        e()
    }
}(),
    function() {
        function e() {
            var e = new Clipboard(".design-asset-copy-as");
            e.on("success", function() {
                $.showMessage("Copied to clipboard!")
            }), e.on("error", function() {
                $.showMessage("Copy failed.")
            })
        }

        function t() {
            $("body").on("click", ".trigger-unsplash-api-guideline", function(e) {
                var t = $(e.target).parent("[data-download-location]").data("download-location");
                t += "?client_id=" + s, $.get(t)
            })
        }

        function n() {
            $("#design-assets-header").on("click", function() {
                $("body").hasClass("layout-side") && $("#design-modal-tabs").slideToggle()
            }), $("#your-files-header").on("click", function() {
                $("body").hasClass("layout-side") && $("#assets-area").slideToggle()
            })
        }
        CP.DesignAssets = {};
        var s = "8e31e45f4a0e8959d456ba2914723451b8262337f75bcea2e04ae535491df16d";
        CP.DesignAssets.init = function() {
            e(), n(), t()
        }
    }(),
    function() {
        function e() {
            $(".design-asset-icon-list > li").each(function(e, t) {
                $(t).attr("data-clipboard-text", $.trim(t.innerHTML)).addClass("design-asset-copy-as")
            })
        }
        CP.IconPicker = {}, CP.IconPicker.init = function() {
            e()
        }
    }(),
    function() {
        function e() {
            $(".design-asset-pattern-list > li").each(function(e, t) {
                $(t).attr("data-clipboard-text", $(t).attr("style")).addClass("design-asset-copy-as")
            })
        }
        CP.PatternPicker = {}, CP.PatternPicker.init = function() {
            e()
        }
    }(),
    function() {
        function e() {
            t("https://api.unsplash.com/photos/random/?count=" + i + "&client_id=" + o)
        }

        function t(e) {
            $.ajax({
                url: e,
                type: "GET"
            }).done(function(e) {
                s(e)
            })
        }

        function n() {
            $("#unsplash-search").on("submit", function(e) {
                e.preventDefault(), t("https://api.unsplash.com/search/photos?page=1&per_page=" + i + "&query=" + $("#unsplash-search-input").val() + "&client_id=" + o)
            })
        }

        function s(e) {
            var t;
            t = e.results ? e.results : e;
            for (var n = "", s = t.length - 1; s >= 0; s--) n += r(t[s]);
            $("#design-asset-photo-list").html(n)
        }
        CP.PhotoPicker = {};
        var i = 10,
            o = "8e31e45f4a0e8959d456ba2914723451b8262337f75bcea2e04ae535491df16d";
        CP.PhotoPicker.init = function() {
            e(), n()
        };
        var r = _.template('<li data-download-location="<%= links.download_location %>"><img src="<%= urls.regular %>" alt="" data-clipboard-text="<%= urls.full %>" class="asset-copy-as trigger-unsplash-api-guideline"><div>By <a href="<%= links.html %>?utm_source=CodePen&utm_medium=referral&utm_campaign=api-credit"><%= user.name %></a><button class="asset-dropdown-arrow button mini-button button-medium editor-dropdown-button" data-dropdown="#photo-dropdown-<%= id %>"><svg class="dropdown-arrow"><use xlink:href="#arrow-down-mini"></use></svg></button><ul id="photo-dropdown-<%= id %>" class="link-list is-dropdown editor-dropdown" data-dropdown-position="css"><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="<%= urls.full %>">Copy URL <span>(Full)</span></a></li><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="<%= urls.small %>">Copy URL <span>(Small)</span></a></li><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="&lt;img src=\'<%= urls.full %>\' alt=\'\'&gt;">Copy as &lt;img&gt; <span>(Full)</span></a></li><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="&lt;img src=\'<%= urls.small %>\' alt=\'\'&gt;">Copy as &lt;img&gt; <span>(Small)</span></a></li><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="background-image: url(<%= urls.full %>);">Copy as background <span>(Full)</span></a></li><li><a href="#0" id="" class="asset-copy-as trigger-unsplash-api-guideline" data-clipboard-text="background-image: url(<%= urls.small %>);">Copy as background <span>(small)</span></a></li></ul></div></li>')
    }();
var MinipageAssets = {
    isAssetsReady: !1,
    isAssetsOpen: !1,
    assetsWrap: $("#assets-wrap"),
    assetsLink: $("#assets-link"),
    popupName: "assets",
    init: function() {
        this.user = __user, this.pageType = "assets_mini", _.extend(this, AJAXUtil), _.extend(this, Assets), this._bindToDOM(), this._bindToHub()
    },
    _bindToHub: function() {
        Hub.sub("asset-added", $.proxy(this._onAssetAdded, this)), Hub.sub("key", $.proxy(this._onKey, this)), Hub.sub("popup-open", $.proxy(this._onPopupOpen, this))
    },
    _bindToDOM: function() {
        this.assetsLink._on("click", this._toggleMiniAssetsView, this)
    },
    _bindAssetEvents: function() {
        var e = this;
        this.allFiles.on("click", ".file-delete", function(t) {
            e.deleteFile(t, this)
        }), this.allFiles.on("mouseenter", ".file-preview", function(t) {
            e.setImagePreviewURL(t, this)
        }), this.allFiles.on("click", ".add-as-external-stylesheet", function(t) {
            e.addAsExternalStylesheet(t, this)
        }), this.allFiles.on("click", ".add-as-external-script", function(t) {
            e.addAsExternalScript(t, this)
        }), $("#assets-area-close-button")._on("click", this._closeAssetsArea, this)
    },
    _toggleMiniAssetsView: function() {
        this.isAssetsOpen ? this._closeAssetsArea() : this._openAssetsArea()
    },
    _closeAssetsArea: function() {
        this.assetsWrap.length && this.assetsWrap.removeClass("open"), this.isAssetsOpen = !1
    },
    _openAssetsArea: function() {
        var e = this;
        this.isAssetsReady ? e.assetsWrap.addClass("open") : $.get("/ajax/assets/bin", function(t) {
            $("#asset-bin-goes-here").replaceWith(t.html), e._getAssets(), setTimeout(function() {
                e.assetsWrap = $("#assets-wrap"), e.assetsWrap.show(), e.assetsWrap.addClass("open"), e.allFiles = $("#your-files-tabs"), CP.ColorPicker.init(), CP.IconPicker.init(), CP.PatternPicker.init(), CP.PhotoPicker.init(), CP.DesignAssets.init(), e.isAssetsReady = !0, e._bindAssetEvents(), SearchFilter.init(), e._bindToDragAndDrop()
            }, 10)
        }), this.isAssetsOpen = !0, Hub.pub("popup-open", this.popupName)
    },
    _getAssets: function() {
        this.get("/uploaded_assets/account_asset", {}, this._doneGetAssets)
    },
    _doneGetAssets: function(e) {
        window.__assets = e.assets, AssetsData.init(), $("#assets-all-files").replaceWith(e.asset_list_html), $("#progress").replaceWith(e.assets_space_indicator_html), $("#assets-area").addClass("files-loaded"), this._setUpCopyButtons()
    },
    _onKey: function(e, t) {
        "esc" === t.key && this._closeAssetsArea()
    },
    _onPopupOpen: function(e, t) {
        t !== this.popupName && this._closeAssetsArea()
    },
    _setUpCopyButtons: function() {
        var e = new Clipboard(".asset-copy-as");
        e.on("success", function() {
            $.showMessage("Copied to clipboard!")
        }), e.on("error", function() {
            $.showMessage("Copying failed.")
        })
    },
    addAsExternalStylesheet: function(e, t) {
        CP.cssSettingsController.resourcesController.quickAddResource("css", $(t).closest(".single-asset").data("asset-url")), $.showMessage("Added as External CSS in Pen Settings!")
    },
    addAsExternalScript: function(e, t) {
        CP.jsSettingsController.resourcesController.quickAddResource("js", $(t).closest(".single-asset").data("asset-url")), $.showMessage("Added as External JavaScript in Pen Settings!")
    },
    deleteFile: function(e, t) {
        e.preventDefault();
        var n = this,
            s = $(t).data("asset-id");
        $.showModal("/ajax/assets/confirm_asset_delete", "modal-warning", function() {
            n._doneConfirmDeleteAsset(s)
        })
    },
    _doneConfirmDeleteAsset: function(e) {
        $("#confirm-delete")._on("click", function() {
            this.finishDeletingAsset(e)
        }, this)
    },
    finishDeletingAsset: function(e) {
        $.hideMessage();
        var t = AssetsData.find(e);
        t["delete"]($.proxy(this.doneDeleteFileAjax, this))
    },
    _uploadComplete: function(e) {
        this._decrementFileCount(), Hub.pub("asset-added", e)
    },
    _onAssetAdded: function(e, t) {
        this._addNewAssetHTML(t), this._resetFileInput($("#manual-file-upload"))
    },
    _addNewAssetHTML: function(e) {
        var t, n = this;
        this._assetElExist(e) ? $("#asset-" + e.asset.id).slideUp(function() {
            $("#asset-" + e.asset.id).remove(), t = $(e.asset_list_html), t.css("transform", "translateY(0)"), t.addClass("newly-added"), n._addAssetToCategory(t, e.asset.category), setTimeout(function() {
                t.removeClass("newly-added"), t.css("transform", "")
            }, 50)
        }) : (t = $(e.asset_list_html), t.addClass("newly-added"), n._addAssetToCategory(t, e.asset.category), setTimeout(function() {
            t.removeClass("newly-added")
        }, 50))
    },
    _assetElExist: function(e) {
        return $("#asset-" + e.asset.id).length > 0
    },
    _addAssetToCategory: function(e, t) {
        $("a[href='#your-files-" + t + "']").click(), e.prependTo("#file-list-" + t), $("#file-list-" + t).find(".no-files").remove()
    },
    _resetFileInput: function(e) {
        e.wrap("<form />").closest("form").get(0).reset(), e.unwrap()
    },
    doneDeleteFileAjax: function(e) {
        AssetsData.del(e.asset.id), this._removeListItem(e.asset.id)
    },
    _removeListItem: function(e) {
        var t = $("#asset-" + e);
        0 === t.siblings().length && t.parent().append("<li class='no-files'>There are no files of this type&nbsp;yet.</li>"), t.addClass("deleting").slideUp(250, function() {
            t.remove()
        })
    },
    setImagePreviewURL: function(e, t) {
        var n = $(t),
            s = n.find(".the-image-preview"),
            i = n.closest(".single-asset").data("asset-url") + "?d=" + Date.now();
        s.css({
            "background-image": "url(" + i + ")"
        })
    }
};
MinipageAssets.init();
var TeamRoomNotifications = Class.extend({
    init: function(e, t) {
        this.user = e, this.rtData = t, this._debouncedCanCollabOnTeam = _.debounce(this._showCanCollabOnTeamPenMessage, 2500), this._debouncedCanCollabSingle = _.debounce(this._showCanCollabOnSingleUserMessage, 2500), this._shouldJoinRoom(t)
    },
    _shouldJoinRoom: function(e) {
        return "editor" === e.role && e.pen.slugHash && (this.user.current_team_id > 0 || this.user.paid === !0)
    },
    _onConnect: function() {
        this._subscribeToServerEvents(), this._publishCanCollabOnTeamPen()
    },
    _subscribeToServerEvents: function() {},
    _amCollabingOnPen: function(e) {
        this.user.id !== e.data.user.id ? this._debouncedCanCollabOnTeam(e) : this._debouncedCanCollabSingle(e)
    },
    _onCanCollabOnTeamPen: function(e) {
        this.user.id !== e.data.user.id ? this._debouncedCanCollabOnTeam(e) : this._debouncedCanCollabSingle(e)
    },
    _showCanCollabOnTeamPenMessage: function(e) {
        var t = "<%= name %> is also working on this pen. Try Collab Mode with your team member <a href='<%= url %>' target='_blank'>here</a>.",
            n = document.location.href.replace(/\/pen\//, "/collab/"),
            s = _.template(t, {
                name: e.data.user.name,
                url: n
            });
        $.showMessage(s, "until-dismiss")
    },
    _showCanCollabOnSingleUserMessage: function(e) {
        var t = "Looks like this Pen is open in another window. You can use <a href='<%= url %>' target='_blank'>Collab mode</a> to make sure you don't override your work.",
            n = document.location.href.replace(/\/pen\//, "/collab/"),
            s = _.template(t, {
                name: e.data.user.name,
                url: n
            });
        $.showMessage(s, "until-dismiss")
    },
    _publishCanCollabOnTeamPen: function() {}
});
"undefined" == typeof window.Copy && (window.Copy = {}), _.extend(window.Copy, {
    penRemovedFromCollection: "Pen removed from Collection.",
    collectionSavedPenAdded: "Your Collection '<%= name %>' was created and this Pen was added to it. <a href='<%= url %>'>View collection</a>.",
    collectionCreated: "Your Collection '<%= name %>' was created. <a href='<%= url %>'>View collection</a>.",
    collectionUpdated: "Your Collection '<%= name %>' has been updated.",
    penAddToCollection: "This Pen was added to the '<%= name %>' collection. <a href='<%= url %>'>View collection</a>.",
    collectionDeleted: "Your Collection was deleted!"
}),
    function() {
        function e() {
            $("body").on("change", ".collection-choice", t)
        }

        function t(e) {
            var t = s(e);
            [c, u].indexOf(t) === -1 && a(t, i(e))
        }

        function n(e) {
            $.showMessage(r(e), "slow"), $(".collections-mini-modal").remove(), d.val(u)
        }

        function s(e) {
            return $(e.target).find("option:selected").val()
        }

        function i(e) {
            return "pen" === l ? CP.pen.slug_hash : $(e.target).closest(".single-pen").data("slug-hash")
        }

        function o(e, t) {
            return "/collections/add/" + e + "/" + t
        }

        function r(e) {
            var t = e.collection.slug_hash;
            return e.collection["private"] && (t = e.collection.slug_hash_private), _.template(Copy.penAddToCollection, {
                name: e.collection.name,
                url: "/collection/" + t
            })
        }

        function a(e, t) {
            AJAXUtil.post(o(e, t), {}, n)
        }
        CP.collectionAddPen = {};
        var c = "__add__",
            u = "__choose__",
            l = window.__pageType,
            d = $("#collection-choice");
        CP.collectionAddPen.init = function() {
            e()
        }
    }(),
    function() {
        function e(e, t) {
            AJAXUtil.put("/collections/" + e.slug_hash, e, t)
        }

        function t(e, t) {
            AJAXUtil.post("/collections", e, t)
        }

        function n(e) {
            return _.extend(e, {
                slug_hash: CP.collection.selectedPenSlugHash
            })
        }

        function s() {
            a({
                "private": d()
            })
        }

        function i() {
            e(CP.collection.VM, function() {
                l()
            })
        }

        function o() {
            return {
                id: "",
                name: "",
                description: "",
                slug_hash: "",
                slug_hash_private: "",
                "private": !1
            }
        }

        function r() {
            CP.collection.VM = o(), CP.collection._VM = [];
            for (var e in CP.collection.VM) CP.collection._VM.push(e)
        }

        function a(e) {
            for (var t in e) CP.collection._VM.indexOf(t) !== -1 && (CP.collection.VM[t] = e[t])
        }

        function c() {
            a(h)
        }

        function u() {
            var e = CP.collection.VM;
            return e["private"] ? e.slug_hash_private : e.slug_hash
        }

        function l() {
            if (window.history.replaceState && u()) {
                var e = URLBuilder.getViewURLSimple("collection", "", u(), !1);
                window.history.replaceState(e, "", e)
            }
        }

        function d() {
            return !!$("#collection-details-private").is(":checked")
        }
        var h = window.__collection;
        CP.collection = {}, CP.collection.init = function() {
            r(), c()
        }, CP.collection.updateViewModel = function(e) {
            a(e)
        }, CP.collection.getActiveSlugHash = u, CP.collection.onPrivacyChange = s, CP.collection.onEditPrivacyChange = i, CP.collection.loadBlankViewModel = r, CP.collection.loadInitialViewModel = c, CP.collection.selectedPenSlugHash = "", CP.collection.save = function(s) {
            CP.collection.VM.slug_hash.length > 0 ? e(CP.collection.VM, s) : t(n(CP.collection.VM), s)
        }
    }(),
    function() {
        function e(e, t) {
            CP.TextFormatter.put("/text_formatter/text", {
                text: e
            }, t)
        }
        CP.TextFormatter = {}, CP.TextFormatter.init = function() {
            _.extend(CP.TextFormatter, AJAXUtil), CP.TextFormatter.formatText = e
        }
    }(),
    function() {
        function e() {
            R.on("click", ".edit-collection", s).on("click", "#add-new-collection-button", n).on("change", ".collection-choice", t).on("submit", ".collection-settings-form", a).on("click", "#add-new-collection-form .ios-toggle", o).on("click", "#edit-collection-form .ios-toggle", r), O.on("click", m)
        }

        function t(e) {
            v(e) === j && (M = !0, i(C(e)))
        }

        function n(e) {
            e.preventDefault(),
                i("")
        }

        function s(e) {
            k = !0, D = !0, e.preventDefault(), P($(e.target).data("slug-hash"), "")
        }

        function i(e) {
            k = !1, D = !1, CP.collection.loadBlankViewModel(), P(0, e)
        }

        function o() {
            CP.collection.onPrivacyChange()
        }

        function r() {
            o(), CP.collection.onEditPrivacyChange(), E()
        }

        function a(e) {
            e.preventDefault(), l()
        }

        function c(e) {
            e.success ? (f(e), g()) : AJAXUtil.showStandardErrorMessage(e)
        }

        function u(e, t) {
            CP.collection.updateViewModel(t), w(e, t), S(t), Hub.pub("collection-saved")
        }

        function l() {
            b(), CP.collection.save(function(e) {
                u(CP.collection.VM.id, e.collection)
            })
        }

        function d() {
            Hub.sub("key", h), Hub.sub("popup-open", p)
        }

        function h(e, t) {
            "esc" === t.key && m()
        }

        function p(e, t) {
            t !== B && m()
        }

        function f(e) {
            A = $("<div />", {
                "class": "modal modal-neutral group ",
                html: e.html
            }), A.appendTo(R)
        }

        function g() {
            CP.showPopupOverlay(), A.find("#name").focus(), V = !0, A.find(".close-button").on("click", m), Hub.pub("popup-open", B), CP.HelpFlyouts.init()
        }

        function m(e) {
            e && e.preventDefault(), V && (CP.hidePopupOverlay(), A.remove(), V = !1)
        }

        function v(e) {
            return $(e.target).find("option:selected").val()
        }

        function C(e) {
            return "pen" === N ? CP.pen.slug_hash : $(e.target).closest(".single-pen").data("slug-hash")
        }

        function b() {
            var e = $(".collection-settings-form").serializeObject();
            CP.collection.updateViewModel(e)
        }

        function y(e, t) {
            var n = Copy.collectionUpdated;
            return D || (n = M ? Copy.collectionSavedPenAdded : Copy.collectionCreated), _.template(n, {
                originalId: e,
                name: t.name,
                url: "/collection/" + CP.collection.getActiveSlugHash()
            })
        }

        function P(e, t) {
            CP.collection.selectedPenSlugHash = t;
            var n;
            n = e ? "/collections/" + e + "/edit?slug_hash=" + t : "/collections/new", $.ajax({
                url: n,
                success: c
            })
        }

        function w(e, t) {
            $.showMessage(y(e, t), "slow")
        }

        function S(e) {
            m(), k && (H.html(e.name), CP.TextFormatter.formatText(e.description, function(e) {
                U.html(e.text), x()
            })), T(e)
        }

        function T(e) {
            var t;
            t = e["private"] === !0 ? F : L, t.append($("<option></option>").attr("value", e.slug_hash).text(e.name)), I.val("__choose__")
        }

        function E() {
            var e = U.find(".private-icon");
            e.length ? e.toggle() : x()
        }

        function x() {
            CP.collection.VM["private"] && U.prepend("<span class='private-icon left'><svg class='icon-lock'><use xlink:href='#lock'></use></svg></span>")
        }
        CP.createEditCollection = {};
        var A, k, M, D, R = $("body"),
            O = $("#popup-overlay"),
            F = $(".collection-options-public"),
            L = $(".collection-options-private"),
            H = $("#collection-name"),
            U = $("#collection-desc"),
            I = $(".collection-choice"),
            j = "__add__",
            B = "createEditCollection",
            V = !1,
            N = window.__pageType;
        CP.createEditCollection.init = function() {
            CP.collection.init(), CP.TextFormatter.init(), e(), d()
        }
    }(),
    function() {
        function e() {
            t()
        }

        function t() {
            $("#bsa-footer").length && $.ajax({
                url: "https://srv.buysellads.com/ads/CKYDVK7U.json?callback=CPAdsinsertAd&forcenads=1&forwardedip=" + __remote_addr,
                dataType: "jsonp"
            })
        }
        CP.Ads = {}, e()
    }(), CPFactory.buildDataObjects(), CPFactory.buildEditorObjects(), CPFactory.buildDesktopViewEditorObjects(), CP.penDelete = new PenDelete, CP.EditorLayout.init(), window.__firebaseData.connectToFirebase && CPFirebase.auth(function() {
    CP.liveRoom = new LiveRoom(window.__rtData, window.__firebaseData), CP.teamRoomNotifications = new TeamRoomNotifications(window.__user, window.__rtData)
}), Hub.pub("page-loading-done"), CP.EditorLayout.doneLoading(), ErrorReporter.init(), CP.createEditCollection.init(), CP.collectionAddPen.init(), CP.cleanEditorURL(), Hub.pub("cm-update-keybindings", {});