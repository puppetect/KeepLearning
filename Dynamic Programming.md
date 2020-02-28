# Dynamic Programming

## Regular Expression

#### Formula
```
          { T[i-1][j-1] } if str[i] == pattern[j] || pattern[j] == '.'
          { T[i][j-2] || T[i-1][j] if (str[i] == pattern[j-1] || pattern[j-1] == '.') } if pattern[j] == '*'
T[i][j] = True if i<0 && j<0
          False otherwise
          
```

#### Sample
```
"ab"
".*"
true

"aab"
"c*a*b"
true

"ab"
".*c"
false

"aab"
"c*a*b"
true

"aaa"
"a*a"
true

"aaa"
"ab*a*c*a"
true

"mississippi"
"mis*is*ip*."
true

"aasdfasdfasdfasdfas"
"aasdf.*asdf.*asdf.*asdf.*s"
true
```